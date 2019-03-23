package tme.pos.BusinessLayer;

import java.io.Serializable;

/**
 * Created by kchoy on 4/22/2015.
 */
public class Duple<T,E> implements Serializable{
    private T t1;
    private E t2;

    public  Duple(T t1,E t2)
    {
        this.t1 = t1;
        this.t2 = t2;
    }
    public T GetFirst(){return t1;}
    public E GetSecond(){return t2;}
    public void SetFirst(T t){t1 = t;}

}
