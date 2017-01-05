package pavelmaca.chat.share;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Lambdas {

    @FunctionalInterface
    public interface Function0 {
        void apply();
    }

    @FunctionalInterface
    public interface Function1<One> {
        void apply(One one);
    }

    @FunctionalInterface
    public interface Function2<One, Two> {
        void apply(One one, Two two);
    }

    @FunctionalInterface
    public interface Function3<One, Two, Three> {
        void apply(One one, Two two, Three three);
    }
}
