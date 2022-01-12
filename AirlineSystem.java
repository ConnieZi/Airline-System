import java.util.*;
import java.io.*;

final public class AirlineSystem implements AirlineInterface {
  private ArrayList<String> cityNames = null;
  private Digraph G = null;
  private static Scanner scan = null;
  private static int INFINITY = Integer.MAX_VALUE;

  /**
   * reads the city names and the routes from a file
   * @param fileName the String file name
   * @return true if routes loaded successfully and false otherwise
   */
  public boolean loadRoutes(String fileName) {
    try {
      Scanner inScan = new Scanner(new FileInputStream(fileName));
      int v = Integer.parseInt(inScan.nextLine());
      G = new Digraph(v);

      cityNames = new ArrayList<String>(v);
      for(int i=0; i<v; i++){
        cityNames.add(inScan.nextLine());
      }

      while(inScan.hasNext()){
        int from = inScan.nextInt();
        int to = inScan.nextInt();
        int weight = inScan.nextInt();
        double price = inScan.nextDouble();
        G.addEdge(new WeightedDirectedEdge(from-1, to-1, weight, price));
        //reversed edge
        G.addEdge(new WeightedDirectedEdge(to-1, from-1, weight, price));
      }
      inScan.close();
      System.out.println("Data imported successfully.");
      System.out.print("Please press ENTER to continue ...");
      return true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * returns the set of city names in the Airline system
   * @return a (possibly empty) Set<String> of city names
   */
  public Set<String> retrieveCityNames() {
    Set<String> cities = new HashSet<String>();

    if(G == null){
      System.out.println("Please import a graph first (option 1).");
      System.out.print("Please press ENTER to continue ...");
      scan = new Scanner(System.in);
      scan.nextLine();
    } else {
      for (int i = 0; i < G.v; i++) {
        cities.add(cityNames.get(i));
      }
    }
    return cities;
  }

  /**
   * returns the set of direct routes out of a given city
   * @param city the String city name
   * @return a (possibly empty) Set<Route> of Route objects representing the
   * direct routes out of city
   * @throws CityNotFoundException if the city is not found in the Airline
   * system
   */
  public Set<Route> retrieveDirectRoutesFrom(String city)
          throws CityNotFoundException {

    //note the return type
    Set<Route> directRoutes = new HashSet<Route>();
    if(G == null){
      System.out.println("Please import a graph first (option 1).");
      System.out.print("Please press ENTER to continue ...");
      return null;
    }

    int cityIn = findIndex(city);
    if(cityIn == -1)
      return null;

    for (WeightedDirectedEdge e : G.adj[cityIn]) {
      directRoutes.add(new Route(cityNames.get(e.from), cityNames.get(e.to), e.weight, e.price ));
    }
    return directRoutes;
  }

  /**
   * finds fewest-stops path(s) between two cities
   * @param source the String source city name
   * @param destination the String destination city name
   * @return a (possibly empty) Set<ArrayList<String>> of fewest-stops pathes. Each path is an
   * ArrayList<String> of city names that includes the source and destination
   * city names.
   * @throws CityNotFoundException if any of the two cities are not found in the
   * Airline system
   */
  //BFS
  public Set<ArrayList<String>> fewestStopsItinerary(String source,
                                                     String destination) throws CityNotFoundException {
    //for the return type
    Set<ArrayList<String>> stopSet = new HashSet<ArrayList<String>>();

    if(!cityNames.contains(source) || !cityNames.contains(destination)) throw new CityNotFoundException("city not found");

    if(G == null){
      System.out.println("Please import a graph first (option 1).");
      System.out.print("Please press ENTER to continue ...");
      scan.nextLine();
    } else {
      int srcIn = findIndex(source);
      int desIn = findIndex(destination);

      G.bfs(srcIn);

      if(!G.marked[desIn]){
        System.out.println("There is no route from " + cityNames.get(srcIn) + " to " + cityNames.get(desIn));
        return null;
      } else {
        Stack<Integer> path = new Stack<>();
        int vertices = 0;
        for (int x = desIn; x != srcIn; x = G.edgeTo[x]){
          path.push(x);
          vertices++;
        }
        path.push(srcIn);

        ArrayList<String> stops = new ArrayList<String>(vertices);

        while(!path.empty()){
          stops.add(cityNames.get(path.pop()));
        }
        //For the return type
        stopSet.add(stops);
      }
    }
    return stopSet;
  }

  /**
   * finds shortest distance path(s) between two cities
   * @param source the String source city name
   * @param destination the String destination city name
   * @return a (possibly empty) Set<ArrayList<String>> of shortest-distance paths. Each path is
   * an ArrayList<Route> of Route objects that includes a Route out of source and into destination.
   * @throws CityNotFoundException if any of the two cities are not found in the
   * Airline system
   */
  //Dijkstra
  public Set<ArrayList<Route>> shortestDistanceItinerary(String source,
                                                         String destination) throws CityNotFoundException {
    Set<ArrayList<Route>> SDSet = new HashSet<ArrayList<Route>>();

    if(!cityNames.contains(source) || !cityNames.contains(destination)) throw new CityNotFoundException("city not found");

    if(G == null){
      System.out.println("Please import a graph first (option 1).");
      System.out.print("Please press ENTER to continue ...");
      scan.nextLine();
    } else {
      int srcIn = findIndex(source);
      int desIn = findIndex(destination);

      G.dijkstras(srcIn, desIn);

      if(!G.marked[desIn]){
        System.out.println("There is no route from " + cityNames.get(srcIn) + " to " + cityNames.get(desIn));
        return SDSet;
      } else {
        Stack<Integer> path = new Stack<>();
        int vertices = 0;
        for (int x = desIn; x != srcIn; x = G.edgeTo[x]){
          path.push(x);
          vertices++;
        }

        ArrayList<Route>  routes = new ArrayList<Route>(vertices);

        int prevVertex = srcIn;
        while(!path.empty()){
          int v = path.pop();
          for (WeightedDirectedEdge e : G.adj[prevVertex]) {
            //add this particular route
            if (e.to == v) {
              routes.add(new Route(cityNames.get(e.from), cityNames.get(e.to), e.weight, e.price ));
              break;
            }
          }
          prevVertex = v;
        }
        SDSet.add(routes);
      }
    }
    return SDSet;
  }

  /**
   * finds shortest distance path(s) between two cities going through
   * a third city
   * @param source the String source city name
   * @param transit the String transit city name
   * @param destination the String destination city name
   * @return a (possibly empty) Set<ArrayList<String>> of shortest-distance paths. Each path is
   * an ArrayList<Route> of Route objects that includes a Route into source, into and out of transit, and
   * into destination.
   * @throws CityNotFoundException if any of the three cities are not found in the
   * Airline system
   */
  public Set<ArrayList<Route>> shortestDistanceItinerary(String source,
                                                         String transit, String destination) throws CityNotFoundException {

    Set<ArrayList<Route>> SDSet = new HashSet<ArrayList<Route>>();
    if(!cityNames.contains(source) ||!cityNames.contains(transit) ||!cityNames.contains(destination)) throw new CityNotFoundException("city not found");

    if(G == null){
      System.out.println("Please import a graph first (option 1).");
      System.out.print("Please press ENTER to continue ...");
      scan.nextLine();
    } else {
      int srcIn = findIndex(source);
      int tranIn= findIndex(transit);
      int desIn = findIndex(destination);


      //ajusted from lab9
      Stack<Integer> cities = new Stack<>();
      cities.push(srcIn);
      cities.push(tranIn);

      int vertices = 0;

      Stack<Integer> path = new Stack<>();
      while (!cities.isEmpty()){
        //int newSrc= tranIn;
        int newSrc= cities.pop();

        G.dijkstras(newSrc, desIn);

        if(!G.marked[desIn]){
          return null;
        } else {
          for (int x = desIn; x != newSrc; x = G.edgeTo[x]){
            path.push(x);
            vertices++;
          }
          desIn = newSrc;
        }
      }

      ArrayList<Route> routes = new ArrayList<Route>(vertices);

      int prevVertex = srcIn;
      while(!path.empty()){
        int v = path.pop();
        //traver the adj list of that city
        for (WeightedDirectedEdge e : G.adj[prevVertex]) {
          if (e.to == v) {
            routes.add(new Route(cityNames.get(e.from), cityNames.get(e.to), e.weight, e.price ));
            break;
          }
        }
        prevVertex = v;
      }
      SDSet.add(routes);
    }
    return SDSet;
  }

  /**
   * finds one Minimum Spanning Tree (MST) for each connected component of
   * the graph
   * @return a (possibly empty) Set<Set<Route>> of MSTs. Each MST is a Set<Route>
   * of Route objects representing the MST edges.
   */
  public Set<Set<Route>> getMSTs(){

    Set<Set<Route>> MSTSet = new HashSet<Set<Route>>();
    Set<Route> routeSet = new HashSet<Route>();

    if(G==null)
    {
      System.out.println("Please import a graph first (option 1).");
      System.out.print("Please press ENTER to continue ...");
      scan.nextLine();
    }
    else {
      PriorityQueue quene = new PriorityQueue<WeightedDirectedEdge>();  // priorityQueue for store the path
      UF uf = new UF(G.v);   // union-find data structure for checking cycle and connection

      //Kruskal
      for (int i = 0; i < G.v; i++)
      {
        for (WeightedDirectedEdge s : G.adj(i)) {
          quene.add(s);    // add all the paths into priority queue
        }
      }
      while (!quene.isEmpty() && routeSet.size() < G.v - 1) {
        WeightedDirectedEdge current = (WeightedDirectedEdge) quene.poll();
        if (!uf.connected(current))  //check if cycles
        {
          uf.union(current);
          Route r = new Route(cityNames.get(current.from), cityNames.get(current.to), current.weight, current.price );
          routeSet.add(r);    //  add it into routeSet
        }
      }
    }
    MSTSet.add(routeSet);
    return MSTSet;
  }

  /**
   * adds a city to the Airline system
   * @param city  the city name
   * @return true if city added successfully and false if the city already exists
   */
  public boolean addCity(String city){

    if(cityNames.contains(city))
      return false;
    cityNames.add(city);  //the size increased automatically
    //a list with NEW SIZE
    LinkedList<WeightedDirectedEdge>[] list = (LinkedList<WeightedDirectedEdge>[]) new LinkedList[cityNames.size()];
    //copy
    for (int i = 0; i< G.v;i++){
      list[i] = G.adj[i];
    }
    //for the last item
    list[G.v] = new LinkedList<WeightedDirectedEdge>();
    G.adj = list;
    G.v++;
    return true;
  }

  /**
   * adds a direct route between two existing cities to the Airline system
   * @param source the source city name
   * @param destination the destination city name
   * @param distance the int distance between the two cities in miles
   * @param price the double ticket price in dollars
   * @return true if route added successfully and false if a route already
   * exists between the two cities
   * @throws CityNotFoundException if any of the two cities are not found in the
   * Airline system
   */
  public boolean addRoute(String source, String destination, int distance,
                          double price) throws CityNotFoundException {
    Set<Route> directRoutes = retrieveDirectRoutesFrom(source);
    //if(DirectRoutes.contains(destination)) return false;
    for(Route r : directRoutes){
      if(r.destination.equals(destination)){
        //route exists
        return false;
      }
    }

    int srcIn = findIndex(source);
    int desIn = findIndex(destination);

    G.addEdge(new WeightedDirectedEdge(srcIn, desIn, distance, price));
    //reversed
    G.addEdge(new WeightedDirectedEdge(desIn, srcIn, distance, price));
    return true;
  }


  /**
   * updates a direct route between two existing cities in the Airline system
   * @param source the String source city name
   * @param destination the String destination city name
   * @param distance the int distance between the two cities in miles
   * @param price the double ticket price in dollars
   * @return true if route updated successfully and false if no route already
   * exists between the two cities
   * @throws CityNotFoundException if any of the two cities are not found in the
   * Airline system
   */
  public boolean updateRoute(String source, String destination, int distance,
                             double price) throws CityNotFoundException {

    if(!cityNames.contains(source) || !cityNames.contains(destination)) throw new CityNotFoundException("City not found");
    int srcIn = findIndex(source);
    int desIn = findIndex(destination);

    //src to des
    for (WeightedDirectedEdge e : G.adj[srcIn]) {
      if(cityNames.get(e.to).equals(destination)){
        e.weight = distance;
        e.price = price;
        break;
      }
    }

    //des to src
    for (WeightedDirectedEdge e : G.adj[desIn]) {
      if(cityNames.get(e.to).equals(source)){
        e.weight = distance;
        e.price = price;
        return true;
      }
    }
    return false;
  }

  private int findIndex(String s) {
    for (int i = 0; i < G.v; i++) {
      if (cityNames.get(i).equals(s))
        return i;
    }
    // not found
    return -1;
  }


  /**
   *  The <tt>Digraph</tt> class represents an directed graph of vertices
   *  named 0 through v-1. It supports the following operations: add an edge to
   *  the graph, iterate over all of edges leaving a vertex.Self-loops are
   *  permitted.
   */
  public class Digraph {
    public int v;
    private int e;
    public LinkedList<WeightedDirectedEdge>[] adj;
    private boolean[] marked;  // marked[v] = is there an s-v path
    private int[] edgeTo;      // edgeTo[v] = previous edge on shortest s-v path
    private int[] distTo;      // distTo[v] = number of edges shortest s-v path
    private double[] priceTo;


    /**
     * Create an empty digraph with v vertices.
     */
    //Code from lab9
    public Digraph(int v) {
      if (v < 0) throw new RuntimeException("Number of vertices must be nonnegative");
      this.v = v;
      this.e = 0;
      @SuppressWarnings("unchecked")
      LinkedList<WeightedDirectedEdge>[] temp =
              (LinkedList<WeightedDirectedEdge>[]) new LinkedList[v];
      adj = temp;
      for (int i = 0; i < v; i++)
        adj[i] = new LinkedList<WeightedDirectedEdge>();
    }

    /**
     * Add the edge e to this digraph.
     */
    public void addEdge(WeightedDirectedEdge edge) {
      int from = edge.from();
      G.adj[from].add(edge);
      G.e++;
    }

    /**
     * Return the edges leaving vertex v as an Iterable.
     * To iterate over the edges leaving vertex v, use foreach notation:
     * <tt>for (WeightedDirectedEdge e : graph.adj(v))</tt>.
     */
    public Iterable<WeightedDirectedEdge> adj(int v) {
      return G.adj[v];
    }

    /**
     *  The <tt>WeightedDirectedEdge</tt> class represents a weighted edge in an directed graph.
     */

    //Code taken from lab9
    public void bfs(int source) {
      marked = new boolean[this.v];
      distTo = new int[this.e];
      edgeTo = new int[this.v];

      Queue<Integer> q = new LinkedList<Integer>();
      for (int i = 0; i < v; i++){
        distTo[i] = INFINITY;
        marked[i] = false;
      }
      distTo[source] = 0;
      marked[source] = true;
      q.add(source);

      while (!q.isEmpty()) {
        int v = q.remove();
        for (WeightedDirectedEdge w : adj(v)) {
          if (!marked[w.to()]) {
            edgeTo[w.to()] = v;
            distTo[w.to()] = distTo[v] + 1;
            marked[w.to()] = true;
            q.add(w.to());
          }
        }
      }
    }

    public void dijkstras(int source, int destination) {
      marked = new boolean[this.v];
      distTo = new int[this.v];
      edgeTo = new int[this.v];

      for (int i = 0; i < v; i++){
        distTo[i] = INFINITY;
        marked[i] = false;
      }
      distTo[source] = 0;
      marked[source] = true;
      int nMarked = 1;

      int current = source;
      while (nMarked < this.v) {
        for (WeightedDirectedEdge w : adj(current)) {
          if (distTo[current]+w.weight() < distTo[w.to()]) {
            edgeTo[w.to()] = current;
            distTo[w.to()] = distTo[current]+w.weight();
          }
        }
        //Find the vertex with minimum path distance
        //This can be done more effiently using a priority queue!
        int min = INFINITY;
        current = -1;

        for(int i=0; i<distTo.length; i++){
          if(marked[i])
            continue;
          if(distTo[i] < min){
            min = distTo[i];
            current = i;
          }
        }
        //Update marked[] and nMarked. Check for disconnected graph.
        if(current != -1 && distTo[current] != INFINITY){
          marked[current] = true ;
          nMarked++;
        } else{
          break;
        }
      }
    }
  } // end class Digraph

  public class WeightedDirectedEdge {
    private final int from;
    private final int to;
    private int weight;
    private double price;
    /**
     * Create a directed edge from v to w with given weight.
     */
    public WeightedDirectedEdge(int from, int to, int weight,double price) {
      this.from = from;
      this.to = to;
      this.weight = weight;
      this.price = price;

    }

    public int from(){
      return this.from;
    }

    public int to(){
      return this.to;
    }

    public int weight(){
      return this.weight;
    }

    public double price(){
      return this.price;
    }
  }

  public class UF  // Union-Find data structure
  {
    public int count;
    public int[] id;

    public UF(int n)
    {
      count=n;
      id = new int[n];
      for(int i=0;i<n;i++)
      {
        id[i]=i;
      }
    }
    public int find(int p)
    {
      return id[p];
    }

    public boolean connected(WeightedDirectedEdge edge)  // check wether connected
    {
      return find(edge.from())==find(edge.to());
    }

    public void union(WeightedDirectedEdge edge)  // union method
    {
      int pID=find(edge.from());
      int qID=find(edge.to());
      if(pID==qID)
        return;

      for(int i=0;i<id.length;i++)
      {
        if(id[i]==pID)
          id[i]=qID;
      }
    }
  }

}
