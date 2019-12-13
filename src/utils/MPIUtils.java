package utils;

import mpi.Datatype;
import mpi.MPI;

import java.util.function.Supplier;

public class MPIUtils {
    public enum DataType {
        INT {
            @Override
            public Datatype get() {
                return MPI.INT;
            }
        };

        public abstract Datatype get();
    }

    public static boolean isMaster() {
        return MPI.COMM_WORLD.Rank() == 0;
    }

    public static int Rank() {
        return MPI.COMM_WORLD.Rank();
    }

    public static int Master() {
        return 0;
    }

    public static int Size() {
        return MPI.COMM_WORLD.Size();
    }

    public static void broadcast(DataType dt, Object buf, int offset, int count, int root) {
        MPI.COMM_WORLD.Bcast(buf, offset, count, dt.get(), root);

    }

    public static void broadcast(DataType dt, Object buf, int offset, int count) {
        broadcast(dt, buf, offset, count, 0);
    }

    public static void scatter(DataType sendDt, Object sendBuf, int sendOffset, int sendCount, DataType receiveDt, Object receiveBuf, int receiveOffset, int receiveCount, int root) {
        MPI.COMM_WORLD.Scatter(sendBuf, sendOffset, sendCount, sendDt.get(), receiveBuf, receiveOffset, receiveCount, receiveDt.get(), root);
    }

    public static void scatter(DataType sendDt, Object sendBuf, int sendOffset, int sendCount, DataType receiveDt, Object receiveBuf, int receiveOffset, int receiveCount) {
        scatter(sendDt, sendBuf, sendOffset, sendCount, receiveDt, receiveBuf, receiveOffset, receiveCount, 0);
    }

    public static void scatter(DataType dt, Object buf, int offset, int count, int root) {
        scatter(dt, buf, offset, count, dt, buf, offset, count, root);
    }

    public static void scatter(DataType dt, Object sendBuf, Object receiveBuf, int offset, int count) {
        scatter(dt, sendBuf, offset, count, dt, receiveBuf, offset, count, 0);
    }
    public static void scatter(DataType dt, Object buf, int offset, int count) {
        scatter(dt, buf, offset, count, dt, buf, offset, count, 0);
    }

    public static void gather(DataType sendDt, Object sendBuf, int sendOffset, int sendCount, DataType receiveDt, Object receiveBuf, int receiveOffset, int receiveCount, int root) {
        MPI.COMM_WORLD.Gather(sendBuf, sendOffset, sendCount, sendDt.get(), receiveBuf, receiveOffset, receiveCount, receiveDt.get(), root);
    }

    public static void gather(DataType sendDt, Object sendBuf, int sendOffset, int sendCount, DataType receiveDt, Object receiveBuf, int receiveOffset, int receiveCount) {
        gather(sendDt, sendBuf, sendOffset, sendCount, receiveDt, receiveBuf, receiveOffset, receiveCount, 0);
    }

    public static void gather(DataType dt, Object buf, int offset, int count, int root) {
        gather(dt, buf, offset, count, dt, buf, offset, count, root);
    }

    public static void gather(DataType dt, Object buf, int offset, int count) {
        gather(dt, buf, offset, count, dt, buf, offset, count, 0);
    }

    public static void master(Supplier<?> code) {
        if (isMaster()) code.get();
    }

    public static double time() {
        return MPI.Wtime();
    }
    public static double jtime() {
        return (System.currentTimeMillis() / 1000.0);
    }
}
