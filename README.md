# Consulable

This project is an experiment showing how you can leverage Consul to form active cluster.

The project comes in two modules.
* lib: a library trying to leverage the Consul API to register the service and listen to new service registrations.
* activemq: an application of the library showing how to use it with ActiveMQ to form a dynamic mesh network of brokers. The test in particular shows how you can add new brokers and send messages to existing subscribers.

This all might be a bad idea:
* The Consul API is awesome in itself. Wrapping it with a lib is an entertaining proposition, but you can probably do this in your own code. Take a look for yourself.
* The application of this library is tailored to the ActiveMQ implementation. Not sure this is going to work well with another active cluster technology. It's unlikely other clusters have such great dynamic capabilities.

