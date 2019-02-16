package com.muflone.wordsolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * Java versions of functions from Python's itertools stdlib module
 * (http://docs.python.org/2/library/itertools.html).
 *
 * @author Jonathan Blakes <jonathan.blakes@gmail.com>
 */
public class Itertools {

    public static <T> List<List<T>>	product(Iterable<? extends Iterable<? extends T>> iterables, int repeat) {
        if (repeat < 0)
            throw new IllegalArgumentException();
        if (repeat == 0)
            return Lists.newArrayList();
//		if (repeat == 1)
////			return Sets.cartesianProduct(iterables);
//			return Lists.cartesianProduct(iterables);
		/*
		def product(*args, **kwds):
		    repeat = kwds.get('repeat', 1)
		    pools = list()
		    for iterable in iterables:
		        pool = list()
		        for element in iterable:
		            pool.append(element)
		        for _ in range(repeat):
		            pools.append(pool)
		*/
        List<List<T>> pools = Lists.newArrayList();
        for (Iterable<? extends T> iterable : iterables) {
            List<T> pool = Lists.newArrayList();
            for (T element : iterable)
                pool.add(element);
            for (int i = 0; i < repeat; i++)
                pools.add(pool);
        }
		/*
	    result = [[]]
	    for pool in pools:
	        tmp = []
	        for x in result:
	            for y in pool:
	                #tmp.append(x+[y])
	                l = list()
	                l.append(y)
	                t = x[:]
	                t.extend(l)
	                tmp.append(t)
	        result = tmp
	    return result
		*/
        List<List<T>> result = Lists.newArrayList();
        result.add(new ArrayList<T>());
        for (List<T> pool : pools) {
            List<List<T>> tmp = Lists.newArrayList();
            for (List<T> x : result)
                for (T y : pool) {
                    List<T> l = Lists.newArrayList();
                    l.add(y);
                    List<T> t = Lists.newArrayList();//x);?
                    for (T element : x)
                        t.add(element);
                    t.addAll(l);
                    tmp.add(t);
					/* surely below does the same:
					List<T> t = Lists.newArrayList(x); // from list
					t.add(y);
					tmp.add(t);
					*/
                }
            result = tmp;
        }
        return result;
    }

    public static <T> List<List<T>>	product(Iterable<? extends Iterable<? extends T>> iterables) {
        return product(iterables, 1);
    }


    public static <T> List<List<T>> permutations(Iterable<? extends T> iterable, int r) {
        if (r < 1)
            throw new IllegalArgumentException();
		/*
		def permutations(iterable, r=None):
		    pool = tuple(iterable)
		    n = len(pool)
		    if r is None:
		    	r = n
		    permutations = []
		    for indices in product(range(n), repeat=r):
		        if len(set(indices)) == n:
		            permutation = []
		            for i in indices:
		                permutation.append(pool[i])
		            permutations.append(permutation)
		    return permutations
		*/
        ImmutableList<T> pool = ImmutableList.copyOf(iterable);
        int n = pool.size();
        List<List<T>> permutations = Lists.newArrayList();
        List<Integer> range = Lists.newArrayList();
        for (int i = 0; i < n; i++)
            range.add(i);
        List<List<Integer>> iterables = ImmutableList.of(range);
        for (List<Integer> indices : product(iterables, r))
            if (ImmutableSet.copyOf(indices).size() == r) {
                List<T> permutation = Lists.newArrayList();
                for (Integer i : indices)
                    permutation.add(pool.get(i));
                permutations.add(permutation);
            }
        return permutations;
    }

    public static <T> List<List<T>> permutations(Collection<? extends T> iterable) {
        return permutations(iterable, iterable.size());
    }

    public static <T> List<List<T>> combinations(Iterable<? extends T> iterable, int r) {
		/*
		http://docs.python.org/2/library/itertools.html#itertools.combinations

		The code for combinations() can be also expressed as a subsequence of
		permutations() after filtering entries where the elements are not in
		sorted order (according to their position in the input pool):

		def combinations(iterable, r):
		    pool = tuple(iterable)
		    n = len(pool)
		    for indices in permutations(range(n), r):
		        if sorted(indices) == list(indices):
		            yield tuple(pool[i] for i in indices)
		*/
        ImmutableList<T> pool = ImmutableList.copyOf(iterable);
        int n = pool.size();
        List<Integer> range = Lists.newArrayList();
        for (int i = 0; i < n; i++)
            range.add(i);
        List<List<T>> combinations = new ArrayList<List<T>>();
        for (List<Integer> indices : permutations(range, r)) {
            List<Integer> sortedIndices = ImmutableList.copyOf(Ordering.natural().sortedCopy(indices));
            if (sortedIndices.equals(indices)) {
                List<T> combination = new ArrayList<T>();
                for (Integer i : indices)
                    combination.add(pool.get(i));
                combinations.add(combination);
            }
        }
        return combinations;
    }

    public static <T> List<List<T>> combinations(Collection<? extends T> collection) {
        return combinations(collection, collection.size());
    }

}
