This sample is a modified version of the code shared here: https://github.com/aspnet/samples/tree/master/samples/aspnetcore/blazor/FlightFinder.  
It has been modified to enable Redis as an IDistributedCache service as well as turning on Session State for the Short List functionality in this sample.  
This was for a demo at Redis Day Seattle in January 2020.  

For those that are interested: In the recorded demo, I forgot to change the code in AppState.cs to call into
 the new ShortList controller, which is why things weren't working at the tail end of the talk.

NOTE: This code assumes that a Redis server instance is running on localhost on port 6379 (Redis' default port).  If you wish to point it
to a different server, you will need to change the Redis configuration in Starup.cs in the Server project.
