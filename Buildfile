require 'buildr/gpg'
require 'buildr/custom_pom'

repositories.remote << 'http://repo1.maven.org/maven2'

repositories.release_to[:url] = 'https://oss.sonatype.org/service/local/staging/deploy/maven2/'
repositories.release_to[:username] = ENV['USERNAME']
repositories.release_to[:password] = ENV['PASSWORD']

VERSION_NUMBER="1.0.0-SNAPSHOT"
  
CONSUL_CLIENT = transitive('com.orbitz.consul:consul-client:jar:0.13.1')

EMBEDDED_CONSUL = transitive('com.pszymczyk.consul:embedded-consul:jar:0.1.10')

ACTIVEMQ_BROKER = transitive('org.apache.activemq:activemq-spring:jar:5.14.1',
                             'org.apache.activemq:activemq-mqtt:jar:5.14.1',
                             'org.springframework:spring-context:jar:4.1.9.RELEASE'
                            )
                            
SLF4J_API = 'org.slf4j:slf4j-api:jar:1.7.21'

LOGBACK = ['ch.qos.logback:logback-classic:jar:1.1.7', 'ch.qos.logback:logback-core:jar:1.1.7']



PAHO = 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:jar:1.1.0'

define('consulable', :group => 'io.tmio', :version => VERSION_NUMBER) do
  define('lib') do
    compile.with CONSUL_CLIENT, SLF4J_API

    package(:jar)
    package(:sources)
    package(:javadoc)
  
    test.with EMBEDDED_CONSUL

  end
  
  define('activemq') do
    compile.with project('consulable:lib'), ACTIVEMQ_BROKER, SLF4J_API
    
    package(:jar)
    package(:sources)
    package(:javadoc)
    
    test.compile.with transitive(project('consulable:lib')), PAHO, LOGBACK
  end
  pom.add_apache_v2_license
  pom.add_github_project('tmio/consulable')
  pom.add_developer('atoulme', 'Antoine Toulme')
end
