package amr.com.routing;

import java.util.List;

interface Parser {
    List<Route> parse() throws RouteException;
}