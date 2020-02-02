using System;
using System.Threading.Tasks; 
using Newtonsoft.Json;
using System.Collections.Generic;
using FlightFinder.Shared;

namespace Microsoft.AspNetCore.Http
{
    public static class ISessionExtensions
    {
        const string shortlistKey = "shortlist";
        public static  IList<Itinerary> GetShortList(this ISession session)
        {
            if (session.IsAvailable)
            {
                var json = session.GetString(shortlistKey);
                
                if (json != null)
                {
                    return JsonConvert.DeserializeObject<IList<Itinerary>>(json);
                }
            }
            
            // nothing in the cache, so return empty list
            return new List<Itinerary>();
        }

        public static async Task SetShortList(this ISession session, IList<Itinerary> list)
        {
            if (list == null || list.Count == 0)
            {
                session.Remove(shortlistKey);
                return;
            }

            var json = JsonConvert.SerializeObject(list);

            session.SetString(shortlistKey, json);

            await session.CommitAsync();
        }
    }
}