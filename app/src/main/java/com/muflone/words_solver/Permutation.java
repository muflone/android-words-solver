package com.muflone.words_solver;

import java.io.StringBufferInputStream;
import java.io.StreamTokenizer;
import java.io.IOException;

public class Permutation {
    final static int MAXN = 16000;   // maximum size of permutation base
    final static int ident_perm[] = {0,  // element zero is ignored!!
            1};

    public static final Permutation identity = new Permutation(ident_perm, 1);

    protected int[] perm;          // vector of permutation images
    protected int   n;             // permutation operates on {1..n}

    /**
     * Create Permutation from integer array.
     */
    Permutation(int[] perm, int n)
    {
        this.perm = new int[n+1];

        this.n = n;

        for(int i=1; i<=n; i++)
            this.perm[i] = perm[i];
    }

    /**
     * Computes the inverse of this permutation.
     */
    Permutation inverse()
    {
        int perm[] = new int[n+1];

        for (int i=1; i<=n; i++)
            perm[this.perm[i]] = i;

        return (new Permutation(perm, this.n));
    }

    /**
     * Creates a copy of this permutation.
     */
    public Object clone()
    {
        return (new Permutation(perm, n));
    }

    /**
     * Create Permutation from string. The constructor assumes lists of mapped integers
     * as the representation.
     */
    public Permutation(String s)
    {
        Permutation tperm;

        int[] tmp, result;
        int tsize, rsize, tok;
        StreamTokenizer st = new StreamTokenizer(new StringBufferInputStream(s));
        st.parseNumbers();

        result = new int[16];
        for (int i=1; i<16; i++) result[i] = i;
        rsize = 0;

        try
        {
            tok = st.nextToken();

            if (tok == st.TT_NUMBER)
            {
                int i = 1;
                do
                {
                    if (tok == '_') // treat as blank => NOP
                    {
                        tok = st.nextToken();
                        continue;
                    }

                    if (st.nval > MAXN)
                        throw new IllegalArgumentException("Permutation: Illegal token '" + st.sval + "' found");
                    else if (i >= result.length  ||  st.nval >= result.length)
                    {
                        tmp = result;
                        result = new int[Math.max(i+16, (int)st.nval+16)];
                        for (int j=1; j<tmp.length; j++)             result[j] = tmp[j];
                        for (int j=tmp.length; j<result.length; j++) result[j] = j;
                    }

                    result[i] = (int)st.nval;
                    if (rsize < i) rsize = i;
                    if (rsize < st.nval) rsize = (int)st.nval;

                    i++;
                    tok = st.nextToken();
                } while (tok == st.TT_NUMBER  ||  tok == '_');
            }
            else
                throw new IllegalArgumentException("Permutation: Illegal token '" + st.sval + "' found");
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Permutation: IOException for '" + s + "'");
        }

        if (tok != st.TT_EOF && tok != st.TT_EOL)
            throw new IllegalArgumentException("Permutation: Illegal token '" + st.sval + "' found");

        perm = new int[rsize+1];
        n = rsize;
        for (int i=1; i<=rsize; i++) this.perm[i] = result[i];

        if (!permOK())
            throw new IllegalArgumentException("Permutation: Syntax check failed for '" + s + "'");
    }

    /**
     * Checks if this permutation is identical to p. Basis sets are adapted if needed.
     */
    boolean equals(Permutation p)
    {
        int i;
        int minn = Math.min(n, p.n);

        for (i=1; i<=minn; i++)
            if (perm[i] != p.perm[i]) return (false);

        // check if extension is identity
        if (n > p.n)
        {
            for (i=minn+1; i<=n; i++) if (perm[i] != i) return (false);
        }
        else
        {
            for (i=minn+1; i<=p.n; i++) if (p.perm[i] != i) return (false);
        }

        return (true);
    }

    /**
     * Returns a string that contains the images of the integer {1..n} under Permutation this.
     */
    public String toString()
    {
        StringBuffer tmp;

        tmp = new StringBuffer(Integer.toString(perm[1]));
        for (int i=2; i<=n; i++)
            tmp.append(" "+perm[i]);

        return (new String(tmp));
    }

    /**
     * Returns the cycle representation of Permutation this. Identities are suppressed.
     */
    String toCycleString()
    {
        StringBuffer tmp;
        boolean visited[] = new boolean[n+1];
        int i, j;

        tmp = null;
        i=1;
        do
        {
            if (perm[i] == i  ||  visited[i])
                i++;
            else
            {
                if (tmp == null)
                    tmp = new StringBuffer("(" + i);
                else
                    tmp.append(" (" + i);
                visited[i] = true;

                j = perm[i];
                while (!visited[j])
                {
                    visited[j] = true;
                    tmp.append(" " + j);
                    j = perm[j];
                };
                tmp.append(")");
                i++;
            }
        } while (i<n);

        if (tmp == null)  // identity
            tmp = new StringBuffer("()");

        // System.err.println("toCycleString(" + toString() + ") = " + tmp);
        return (new String(tmp));
    }

    /**
     * This method implements the right multiply of this with right_perm
     * as used in C. M. Hoffmann, Lecture Notes in Computer Science, Vol. 136.
     */
    public Permutation times(Permutation right_perm)
    {
        int maxn, tmp;
        maxn = Math.max(this.n, right_perm.n);
        int result_array[] = new int[maxn+1];

        for (int i=1; i<=maxn; i++)
        {
            if (this.n < i)   // extend right_perm
                tmp = i;
            else
                tmp = this.perm[i];

            if (right_perm.n < tmp)       // extend this
                result_array[i] = tmp;
            else
                result_array[i] = right_perm.perm[tmp];
        }

        return (new Permutation(result_array, maxn));
    }

    /**
     * This method implements the right multiply of this with right_perm
     * as shown in "dtv-Atlas zur Mathematik". There seems to be no standard
     * way of doing this.
     */
    public Permutation dtv_times(Permutation right_perm)
    {
        int maxn, tmp;
        maxn = Math.max(this.n, right_perm.n);
        int result_array[] = new int[maxn+1];

        for (int i=1; i<=maxn; i++)
        {
            if (right_perm.n < i)   // extend right_perm
                tmp = i;
            else
                tmp = right_perm.perm[i];

            if (this.n < tmp)       // extend this
                result_array[i] = tmp;
            else
                result_array[i] = this.perm[tmp];
        }

        return (new Permutation(result_array, maxn));
    }

    /**
     * Tests this permutation for internal consistency. Returns true if checks were passed.
     */
    public boolean permOK()
    {
        int counts[] = new int[n+1];
        int i;

        for (i=1; i<=n; i++)
            if (perm[i] > n) return (false);
            else             counts[perm[i]]++;

        for (i=1; i<=n; i++)
            if (counts[i] != 1) return (false);

        return (true);
    }
}
