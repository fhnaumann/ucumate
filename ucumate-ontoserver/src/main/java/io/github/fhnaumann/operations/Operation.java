package io.github.fhnaumann.operations;

/**
 * @author Felix Naumann
 */
public interface Operation<IN, OUT> {

    OUT perform(IN in);
}
