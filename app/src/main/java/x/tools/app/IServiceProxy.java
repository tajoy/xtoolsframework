package x.tools.app;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface IServiceProxy {
    void call_1();
    void call_2(int arg1, long arg2, double arg3, String arg4);
    boolean call_3(int[] arg1);
    int call_4(long[] arg1);
    String call_5(double[] arg1);

    boolean call_6(List<Integer> arg1);
    int call_7(List<Long> arg1);
    String call_8(Map<String, String> arg1);
    Map<String, String> call_9(Map<String, Map<String, String>> arg1);

    class Data {
        public int i;
        public long l;
        public double d;
        public Boolean b;
        public String string;

        public Data(int i, long l, double d, Boolean b, String string) {
            this.i = i;
            this.l = l;
            this.d = d;
            this.b = b;
            this.string = string;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "i=" + i +
                    ", l=" + l +
                    ", d=" + d +
                    ", b=" + b +
                    ", string='" + string + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Data)) return false;
            Data data1 = (Data) o;
            return i == data1.i &&
                    l == data1.l &&
                    Double.compare(data1.d, d) == 0 &&
                    Objects.equals(b, data1.b) &&
                    Objects.equals(string, data1.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, l, d, b, string);
        }
    }
    Data call_10(Data arg1);
    Map<String, Data> call_11(Map<String, Data> arg1);

    Object call_12(Object ... args);
}
