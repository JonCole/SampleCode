using System;
using Newtonsoft.Json;
using Microsoft.Extensions.Caching.Distributed;
using System.Threading.Tasks;
using System.Collections.Generic;
using FlightFinder.Shared;

namespace Microsoft.Extensions.Caching.Distributed
{
    public static class IDistributedCacheExtensions
    {
        public static async Task<IList<Itinerary>> GetSearchResultsAsync(this IDistributedCache cache, string searchId)
        {
            return await cache.GetAsync<IList<Itinerary>>(searchId);
        }

        public static async Task AddSearchResultsAsync(this IDistributedCache cache, string searchId, IList<Itinerary> flights)
        {
            var options = new DistributedCacheEntryOptions();
            options.SlidingExpiration = TimeSpan.FromSeconds(10);
            options.AbsoluteExpirationRelativeToNow = TimeSpan.FromSeconds(30);
            await cache.SetAsync(searchId, flights, options);
        }

        public static async Task<T> GetAsync<T>(this IDistributedCache cache, string key) where T : class
        {
            var json = await cache.GetStringAsync(key);
            if (json == null)
                return null;

            return JsonConvert.DeserializeObject<T>(json);
        }

        public static async Task SetAsync<T>(this IDistributedCache cache, string key, T value, DistributedCacheEntryOptions options) where T : class
        {
            var json = JsonConvert.SerializeObject(value);
            await cache.SetStringAsync(key, json, options);
        }
    }
}