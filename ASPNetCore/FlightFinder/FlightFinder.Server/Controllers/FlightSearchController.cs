using FlightFinder.Shared;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.Extensions.Caching.Distributed;

namespace FlightFinder.Server.Controllers
{
    [Route("api/[controller]")]
    public class FlightSearchController
    {
        private readonly IDistributedCache cache;
        public FlightSearchController(IDistributedCache c)
        {
            cache = c;
        }

        // public async Task<IList<Itinerary>> Search([FromBody] SearchCriteria criteria)
        // {
        //     var flights = await FlightDataSource.FindFlightsAsync(criteria);
		// 	return flights;
        // }

        public async Task<IList<Itinerary>> Search([FromBody] SearchCriteria criteria)
        {
            // returns something like "FlightFinder/Search/LHR/SEA/2020-07-02.00:00:00Z/2020-07-09.00:00:00Z/Economy"
            var searchId = criteria.GetSearchId();

            var flights = await cache.GetSearchResultsAsync(searchId);

            if (flights == null)
            {
                flights = await FlightDataSource.FindFlightsAsync(criteria);

                await cache.AddSearchResultsAsync(searchId, flights);
            }

            return flights;
        }
    }
}
