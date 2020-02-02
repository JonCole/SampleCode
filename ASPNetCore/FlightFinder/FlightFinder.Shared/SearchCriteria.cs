using System;

namespace FlightFinder.Shared
{
    public class SearchCriteria
    {
        public string FromAirport { get; set; }
        public string ToAirport { get; set; }
        public DateTime OutboundDate { get; set; }
        public DateTime ReturnDate { get; set; }
        public TicketClass TicketClass { get; set; }

        public SearchCriteria()
        {
        }

        public SearchCriteria(string fromAirport, string toAirport) : this()
        {
            FromAirport = fromAirport;
            ToAirport = toAirport;
            OutboundDate = new DateTime(2020, 7, 2).Date;
            ReturnDate = OutboundDate.AddDays(7);
        }

        public string GetSearchId()
        {
            return $"Search/{FromAirport}/{ToAirport}/{FormatDate(OutboundDate)}/{FormatDate(ReturnDate)}/{TicketClass}";
        }

        private static string FormatDate(DateTime d) => d.ToString("u").Replace(" ", ".");

    }
}
