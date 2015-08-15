package de.donxs.pinghandler.callback;

public interface Callback<V> {
    
    public void done(V value, Throwable throwable);
    
}
