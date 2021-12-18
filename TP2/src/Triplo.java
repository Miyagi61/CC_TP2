public class Triplo<T, U, V> {
    public final T t;
    public final U u;
    public final V v;

    public Triplo(T t, U u, V v) {
        this.t = t;
        this.u = u;
        this.v = v;
    }

    public T getFst() {
        return t;
    }

    public U getSnd() {
        return u;
    }

    public V getTrd(){
        return v;
    }
}
