package com.example.proyecto.date

import com.google.firebase.database.ServerValue
import com.google.type.DateTime
import java.time.Instant
import kotlin.math.floor

class PrettyDate {

    class PrettyDataFormater(
        val threshold: Long = 0,
        val handler: (diff:Long)->String
    )

    private fun createHandler(div:Long, noun:String, rest:String) : (diff:Long)->String{
        return {
            val n = it / div
            val pluralizedNoun = noun + when(n > 1){
                true -> 's'
                false -> Character.MIN_VALUE
            }
            "$n $pluralizedNoun $rest"
        }
    }

    private val formaters = listOf(
        PrettyDataFormater(-31535999, createHandler(-31536000, "year", "from now")),
        PrettyDataFormater(-2591999, createHandler(-2592000, "month", "from now")),
        PrettyDataFormater(-604799, createHandler(-604800, "week", "from now")),
        PrettyDataFormater(-172799, createHandler(-86400, "day", "from now")),
        PrettyDataFormater(-3599, createHandler(-3600, "hour", "from now")),
        PrettyDataFormater(-59, createHandler(-60, "minute", "from now")),
//        PrettyDataFormater(-1, createHandler(1, "second", "from now")),
        PrettyDataFormater( 60, createHandler(1, "second", "ago")),
        PrettyDataFormater(3600, createHandler(60, "minute", "ago")),
        PrettyDataFormater(86400, createHandler(3600, "hour", "ago")),
        PrettyDataFormater(172800, createHandler(86400, "yesterday", "")),
        PrettyDataFormater(604800, createHandler(86400, "day", "ago")),
        PrettyDataFormater(2592000, createHandler(604800, "week", "ago")),
        PrettyDataFormater(31536000, createHandler(2592000, "month", "ago")),
        PrettyDataFormater(Long.MAX_VALUE, createHandler(31536000, "year", "ago")),
    )

    fun format(timestamp: Long):String{
        val now = Instant.now().epochSecond
        val diff = (now - timestamp/1000)
        for(formater in formaters){
            if(diff < formater.threshold){
                return formater.handler(diff);
            }
        }
        return ""
    }

}

/*

var formatters = [
	{ threshold: -31535999, handler: createHandler(-31536000,	"year",     "from now" ) },
	{ threshold: -2591999, 	handler: createHandler(-2592000,  	"month",    "from now" ) },
	{ threshold: -604799,  	handler: createHandler(-604800,   	"week",     "from now" ) },
	{ threshold: -172799,   handler: createHandler(-86400,    	"day",      "from now" ) },
	{ threshold: -86399,   	handler: function(){ return      	"tomorrow" } },
	{ threshold: -3599,    	handler: createHandler(-3600,     	"hour",     "from now" ) },
	{ threshold: -59,     	handler: createHandler(-60,       	"minute",   "from now" ) },
	{ threshold: -0.9999,   handler: createHandler(-1,			"second",   "from now" ) },
	{ threshold: 1,        	handler: function(){ return      	"just now" } },
	{ threshold: 60,       	handler: createHandler(1,        	"second",	"ago" ) },
	{ threshold: 3600,     	handler: createHandler(60,       	"minute",	"ago" ) },
	{ threshold: 86400,    	handler: createHandler(3600,     	"hour",     "ago" ) },
	{ threshold: 172800,   	handler: function(){ return      	"yesterday" } },
	{ threshold: 604800,   	handler: createHandler(86400,    	"day",      "ago" ) },
	{ threshold: 2592000,  	handler: createHandler(604800,   	"week",     "ago" ) },
	{ threshold: 31536000, 	handler: createHandler(2592000,  	"month",    "ago" ) },
	{ threshold: Infinity, 	handler: createHandler(31536000, 	"year",     "ago" ) }
];

var prettydate = {
	format: function (date) {
		var diff = (((new Date()).getTime() - date.getTime()) / 1000);
		for( var i=0; i<formatters.length; i++ ){
			if( diff < formatters[i].threshold ){
				return formatters[i].handler(diff);
			}
		}
		throw new Error("exhausted all formatter options, none found"); //should never be reached
	}
}
 */