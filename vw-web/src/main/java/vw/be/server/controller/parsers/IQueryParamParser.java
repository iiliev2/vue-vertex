package vw.be.server.controller.parsers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import vw.be.server.exceptions.MalformedQueryException;

/**
 * Represents an HTTP URI query parameter parser. It converts from one input
 * type to another. Can be used also as a filter/predicate, where the input is
 * the same as the output. Several simple parsers can be chained to produce a
 * single, more complex parser. Throws a {@link MalformedQueryException} in case
 * the operation cannot be performed.
 *
 * @param <I>
 *            input
 * @param <O>
 *            output
 */
@FunctionalInterface
public interface IQueryParamParser<I, O> {
	/**
	 * Build a {@link IQueryParamParser} which will filer any elements from a
	 * collection, based on the provided predicate.
	 */
	Function<Predicate<String>, IQueryParamParser<Collection<String>, Collection<String>>> COLLECTION_FILTER_BUILDER =
			predicate -> collection -> collection =
					collection.stream().filter(predicate).collect(Collectors.toSet());

	/**
	 * Build a {@link IQueryParamParser} which will validate a collection using
	 * the provided consumer. In case it fails, the consumer is expected to
	 * throw a runtime exception, which will then be wrapped by a
	 * {@link MalformedQueryException}.
	 */
	Function<Consumer<Collection<String>>, IQueryParamParser<Collection<String>, Collection<String>>> COLLECTION_VALIDATOR_BUILDER =
			consumer -> collection -> {
				try {
					consumer.accept(collection);
				} catch (Throwable e) {
					throw new MalformedQueryException("The collection does not satisfy the criteria",
							e);
				}
				return collection;
			};

	/**
	 * Build a {@link IQueryParamParser} which will validate each element in a
	 * collection using the provided consumer. In case it fails, the consumer is
	 * expected to throw a runtime exception, which will then be wrapped by a
	 * {@link MalformedQueryException}.
	 */
	Function<Consumer<String>, IQueryParamParser<Collection<String>, Collection<String>>> COLLECTION_ELEMENT_VALIDATOR_BUILDER =
			consumer -> collection -> {
				try {
					collection.stream().forEach(consumer);
				} catch (Throwable e) {
					throw new MalformedQueryException(
							"At least one of the elements does not satisfy the criteria",
							e);
				}
				return collection;
			};

	/** Takes a string and produces an array of tokens, split on commas */
	IQueryParamParser<String, String[]> SPLIT_ON_COMMAS = paramValue -> StringUtils.split(",");

	/**
	 * Converts an array of strings to a collection and trims leading and
	 * trailing spaces from each string
	 */
	IQueryParamParser<String[], Collection<String>> TRIM_AND_COLLECT = parsedValues -> {
		Set<String> result = new HashSet<>();
		Collections.addAll(result, parsedValues);
		result.stream().forEach(parsedValue -> parsedValue.trim());
		return result;
	};

	/** Filters any empty strings from the collection */
	IQueryParamParser<Collection<String>, Collection<String>> FILTER_EMPTY_VALUES = COLLECTION_FILTER_BUILDER
			.apply(StringUtils::isEmpty);

	/** Will throw an exception, if the collection is empty */
	IQueryParamParser<Collection<String>, Collection<String>> NOT_EMPTY_COLLECTION =
			COLLECTION_VALIDATOR_BUILDER.apply(collection -> {
				if (collection.isEmpty())
					throw new RuntimeException("The collection is empty");
			});

	/**
	 * Will throw an exception, if the collection is not exactly of two elements
	 */
	IQueryParamParser<Collection<String>, Collection<String>> IS_A_PAIR =
			COLLECTION_VALIDATOR_BUILDER.apply(collection -> {
				if (collection.isEmpty())
					throw new RuntimeException("The collection must contain exactly two elements");
			});

	/**
	 * Will throw an exception, unless the collection contains only positive
	 * ints
	 */
	IQueryParamParser<Collection<String>, Collection<String>> CHECK_POSITIVE_INT_TYPE =
			COLLECTION_ELEMENT_VALIDATOR_BUILDER
					.apply(Integer::parseUnsignedInt);

	/**
	 * Parses a string to a non-empty, comma separated collection of positive
	 * integers
	 */
	IQueryParamParser<String, Collection<String>> COMMA_SEPARATED_LIST_PARSER =
			param -> CHECK_POSITIVE_INT_TYPE.apply(
					NOT_EMPTY_COLLECTION.apply(
							FILTER_EMPTY_VALUES.apply(
									TRIM_AND_COLLECT.apply(
											SPLIT_ON_COMMAS.apply(param)))));

	/** Parses a string to a two element collection of positive integers */
	IQueryParamParser<String, Collection<String>> COMMA_SEPARATED_RANGE_PARSER =
			param -> CHECK_POSITIVE_INT_TYPE.apply(
					IS_A_PAIR.apply(
							FILTER_EMPTY_VALUES.apply(
									TRIM_AND_COLLECT.apply(
											SPLIT_ON_COMMAS.apply(param)))));

	/**
	 * Converts, filters or performs some other arbitrary processing on the
	 * input. Depending on the types of the input and output, it could serve
	 * either as a function, filter or predicate.
	 *
	 * @param input
	 * @return output
	 * @throws MalformedQueryException
	 *             in case the operation cannot be performed
	 */
	O apply(I input) throws MalformedQueryException;
}
