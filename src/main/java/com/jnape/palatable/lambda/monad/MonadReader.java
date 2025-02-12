package com.jnape.palatable.lambda.monad;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.Contravariant;
import com.jnape.palatable.lambda.functor.Profunctor;
import com.jnape.palatable.lambda.functor.builtin.Lazy;

/**
 * A monad that is capable of reading an environment <code>R</code> and producing a lifted value <code>A</code>. This
 * is strictly less powerful than an {@link Fn1}, loosening the requirement on {@link Contravariant} (and therefore
 * {@link Profunctor} constraints), so is more generally applicable, offering instead a {@link MonadReader#local(Fn1)}
 * mechanism for modifying the environment *after* reading it but before running the effect (as opposed to
 * {@link Contravariant} functors which may modify their inputs *before* running their effects, and may therefore alter
 * the input types).
 *
 * @param <R>  the environment
 * @param <A>  the output
 * @param <MR> the witness
 */
public interface MonadReader<R, A, MR extends MonadReader<R, ?, MR>> extends Monad<A, MR> {

    /**
     * Modify this {@link MonadReader MonadReader's} environment after reading it but before running the effect.
     *
     * @param fn the modification function
     * @return the {@link MonadReader} with a modified environment
     */
    MonadReader<R, A, MR> local(Fn1<? super R, ? extends R> fn);

    /**
     * {@inheritDoc}
     */
    @Override
    <B> MonadReader<R, B, MR> flatMap(Fn1<? super A, ? extends Monad<B, MR>> f);

    /**
     * {@inheritDoc}
     */
    @Override
    <B> MonadReader<R, B, MR> pure(B b);

    /**
     * Using both the environment and the current output, chain dependent
     * computations that may continue or short-circuit based on previous results.
     *
     * @param fn  the dependent computation over R and A
     * @param <B> the resulting Reader parameter type
     * @return the new Reader instance
     */
    <B> MonadReader<R, B, MR> rflatMap(Fn2<? super R, ? super A, ? extends MonadReader<R, B, MR>> fn);

    /**
     * Using both the environment and the current output, chain dependent
     * computations that may continue or short-circuit based on previous results.
     *
     * @param fn  the dependent computation over R and A
     * @param <B> the resulting Reader parameter type
     * @return the new Reader instance
     */
    default <B> MonadReader<R, B, MR> rflatMap(Fn1<Tuple2<? super A, ? super R>, ? extends MonadReader<R, B, MR>> fn) {
        return rflatMap((r, a) -> fn.apply(tuple(a, r)));
    }

    /**
     * Map both the environment and the current output to a new output.
     *
     * @param fn  the mapping function
     * @param <B> the new state type
     * @return the mapped {@link MonadReader}
     */
    default <B> MonadReader<R, B, MR> rmap(Fn2<? super R, ? super A, ? extends B> fn) {
        return rflatMap((r, a) -> pure(fn.apply(r, a)));
    }

    /**
     * Map both the environment and the current output to a new output.
     *
     * @param fn  the mapping function
     * @param <B> the new state type
     * @return the mapped {@link MonadReader}
     */
    default <B> MonadReader<R, B, MR> rmap(Fn1<Tuple2<? super A, ? super R>, ? extends B> fn) {
        return rflatMap((r, a) -> pure(fn.apply(tuple(a, r))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadReader<R, B, MR> fmap(Fn1<? super A, ? extends B> fn) {
        return Monad.super.<B>fmap(fn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadReader<R, B, MR> zip(Applicative<Fn1<? super A, ? extends B>, MR> appFn) {
        return Monad.super.zip(appFn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> Lazy<? extends MonadReader<R, B, MR>> lazyZip(
            Lazy<? extends Applicative<Fn1<? super A, ? extends B>, MR>> lazyAppFn) {
        return Monad.super.lazyZip(lazyAppFn).fmap(Applicative<B, MR>::coerce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadReader<R, B, MR> discardL(Applicative<B, MR> appB) {
        return Monad.super.discardL(appB).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadReader<R, A, MR> discardR(Applicative<B, MR> appB) {
        return Monad.super.discardR(appB).coerce();
    }
}
