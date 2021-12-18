public class Par<T, U> {
    public final T t;
    public final U u;

    public Par(T t, U u) {
        this.t= t;
        this.u= u;
    }

    public T getFst(){
        return t;
    }
    public U getSnd(){
        return u;
    }
}

