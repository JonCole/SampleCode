
# python -m pip install redis-py-cluster

import time, random, string
from rediscluster import StrictRedisCluster
from rediscluster.client import ClusterConnectionPool

host = "XXX"
password = ""
shardMap = None
numShards = 2
maxSlots = 16384
numKeys = 400

def randomword(length):
   return ''.join(random.choice(string.ascii_lowercase) for i in range(length))

def generateKeys(count):
    keys = []
    for i in range(count):
        keys.append(randomword(10))
    return keys

def getShardFromSlot(slot, shardMap):
    if shardMap is None:
        shardMap = []
        slotsPerShard = int(maxSlots / numShards)
        currentShardMaxSlot = slotsPerShard
        for i in range(numShards):
            if i == numShards-1:
                shardMap.append(maxSlots)
            else:
                shardMap.append(currentShardMaxSlot)
                currentShardMaxSlot += slotsPerShard
    for i in range(len(shardMap)):
        if slot <= shardMap[i]:
            return i

redisSettings = {
   'socket_timeout': 1, 
    'socket_connect_timeout': 30,
    'socket_keepalive': True,
    'password': password,
    'skip_full_coverage_check': True,    
}

port = 6379
ssl = False

if ssl == True:
    redisSettings["ssl"] =  True
    port = 6380

startup_nodes = [{'host': host, 'port': port}]

print("Creating StrictRedisCluster for " + host + ", port=" + str(port) + " with connectTimout of " + str(redisSettings['socket_connect_timeout']))

pool = ClusterConnectionPool(startup_nodes=startup_nodes, max_connections_per_node=10, **redisSettings)
client = StrictRedisCluster(connection_pool=pool)
#client = StrictRedisCluster(startup_nodes=startup_nodes, **redisSettings)

keys = generateKeys(numKeys)

#print(keys)

shardCounts = [0, 0]
opCount = 0
for k in keys:
    client.set(k, random.choice(keys))
    slot = client.cluster_keyslot(k)
    shardId = getShardFromSlot(slot, shardMap)
    shardCounts[shardId] += 1
    opCount += 1
    shardPercent = int(round(shardCounts[shardId] / opCount, 2) * 100)

    print("calling Redis.Set(" + k + ") => shard:" + str(shardId) + ", ShardPercent:" + str(shardPercent) + "%, slot:" + str(slot))
    

shardCounts = [0, 0]
opCount = 0
while True:
    try:
        k = random.choice(keys)
        slot = client.cluster_keyslot(k)
        shardId = getShardFromSlot(slot, shardMap)
        shardCounts[shardId] += 1
        opCount += 1
        shardPercent = int(round(shardCounts[shardId] / opCount, 2) * 100)
        print("calling Redis.Get(" + k + ") => shard:" + str(shardId) + ", ShardPercent:" + str(shardPercent) + "%, slot:" + str(slot))
        client.get(k)
    except Exception as ex:
        print("Error: Failed to get key " + k + ".  **********************************" + ex)
        

    #print("Sleeping...")
    #time.sleep(2)
    print("")






