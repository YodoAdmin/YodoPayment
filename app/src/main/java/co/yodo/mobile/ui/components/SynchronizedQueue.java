package co.yodo.mobile.ui.components;

/**
 * Helper class for containing frames. Frames got from camera are put to the queue, and
 * the same queue is read by face analysis thread.
 */
class SynchronizedQueue<V> {
    private Object[] elements;
    private int head;
    private int tail;
    public int size;

    SynchronizedQueue( int capacity ) {
        elements = new Object[ capacity ];
        head = 0;
        tail = 0;
        size = 0;
    }

    synchronized V remove() throws InterruptedException {
        while( size == 0 ) {
            wait();
        }

        if( size==0 ) {
            return null;
        }

        @SuppressWarnings( "unchecked" )
        V r = (V) elements[ head ];
        head++;
        size--;

        if( head == elements.length ) {
            head = 0;
        }

        notifyAll();
        return r;
    }

    public synchronized void add( V newValue ) throws InterruptedException {
        while( size == elements.length ) {
            wait();
        }

        elements[ tail ] = newValue;
        tail++;
        size++;

        if( tail == elements.length ) {
            tail = 0;
        }
        notifyAll();
    }
}