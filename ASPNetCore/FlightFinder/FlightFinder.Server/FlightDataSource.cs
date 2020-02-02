using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using FlightFinder.Shared;


namespace FlightFinder.Server
{
    public static class FlightDataSource
    {
        public static async Task<IList<Itinerary>> FindFlightsAsync(SearchCriteria criteria)
        {
            await Task.Delay(500);//look busy
            var rng = new Random();
            return Enumerable.Range(0, rng.Next(2, 5)).Select(_ => new Itinerary
            {
                Price = rng.Next(100, 2000),
                Outbound = new FlightSegment
                {
                    Airline = RandomAirline(),
                    FromAirportCode = criteria.FromAirport,
                    ToAirportCode = criteria.ToAirport,
                    DepartureTime = criteria.OutboundDate.AddHours(rng.Next(24)).AddMinutes(5 * rng.Next(12)),
                    ReturnTime = criteria.OutboundDate.AddHours(rng.Next(24)).AddMinutes(5 * rng.Next(12)),
                    DurationHours = 2 + rng.Next(10),
                    TicketClass = criteria.TicketClass
                },
                Return = new FlightSegment
                {
                    Airline = RandomAirline(),
                    FromAirportCode = criteria.ToAirport,
                    ToAirportCode = criteria.FromAirport,
                    DepartureTime = criteria.ReturnDate.AddHours(rng.Next(24)).AddMinutes(5 * rng.Next(12)),
                    ReturnTime = criteria.ReturnDate.AddHours(rng.Next(24)).AddMinutes(5 * rng.Next(12)),
                    DurationHours = 2 + rng.Next(10),
                    TicketClass = criteria.TicketClass
                },
            })
            .OrderBy(e => e.Price)
            .ToList();
        }
        
        private static string RandomAirline()
            => SampleData.Airlines[new Random().Next(SampleData.Airlines.Length)];
    }
}
