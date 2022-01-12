import java.util.*;
@SuppressWarnings("unchecked")
public class Graph{
    public final int V;
    public int E;
    //public Set<Route>[] adj;
    public LinkedList<Route>[] adj;

    public Graph(int V){
        this.V = V;
        this.E = 0;

        /**
         Set<Route>[] temp = (Set<Route>[]) new Set[V];
         adj = temp;
         for(int i=0; i<V; i++) adj[i] = new HashSet<Route>();
         */
        LinkedList<Route>[] temp = (LinkedList<Route>[]) new LinkedList[V];
        adj = temp;
        for(int i=0; i<V; i++) adj[i] = new LinkedList<Route>();

    }
    public Graph(Graph old, int V){
        this.V = V;
        this.E = old.E;
        /**
         Set<Route>[] temp = (Set<Route>[]) new Set[V];
         adj = temp;
         for(int i=0; i<old.V; i++) adj[i] = old.naAdj(i);
         for(int i=old.V; i<V; i++ ) adj[i] = new HashSet<Route>();
         */
        LinkedList<Route>[] temp = (LinkedList<Route>[]) new LinkedList[V];
        adj = temp;
        for(int i=0; i<V; i++) adj[i] = old.naAdj(i);
        for(int i=old.V; i<V; i++ ) adj[i] = new LinkedList<Route>();


    }
    public void addEdge(int v, int w, Route r){
        adj[v].add(r);
        //adj[w].add(r);
        E++;
    }
    public void deleteEdge(int v, int w, Route r){
        adj[v].remove(r);
        adj[w].remove(r);
    }
    public Iterable<Route> adj(int v){
        return adj[v];
    }
    public LinkedList<Route> naAdj(int v){
        return adj[v];
    }

}
