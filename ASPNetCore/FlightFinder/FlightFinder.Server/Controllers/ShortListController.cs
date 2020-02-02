using FlightFinder.Shared;
using Microsoft.AspNetCore.Mvc;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;

namespace FlightFinder.Server.Controllers
{
    [Route("api/[controller]")]
    public class ShortList : Controller
    {
        [Route("get")]
        public IList<Itinerary> Get()
        {
            var result = this.HttpContext.Session.GetShortList();
            return result;
        }

        [Route("add")]
        public async Task<IList<Itinerary>> Add([FromBody]Itinerary item)
        {
            var list = this.HttpContext.Session.GetShortList();

            list.Add(item);
            
            await this.HttpContext.Session.SetShortList(list);

            return list;
        }

        [Route("remove")]
        public async Task<IList<Itinerary>> Remove([FromBody]Itinerary item)
        {
            var list = this.HttpContext.Session.GetShortList();

            foreach(var i in list)
            {
                // Real app would need better logic here.
                if (i.AirlineName == item.AirlineName
                    && i.Price == item.Price)  
                {
                    list.Remove(i); 
                    break;
                }
            }            
            
            await this.HttpContext.Session.SetShortList(list);
            return Get();
        }
    }
}
