package e;

import java.io.Serializable;

/**
 * Serialization compatibility shell for the legacy .ban player record.
 * Field names and serialVersionUID intentionally match the legacy byte stream.
 * Do not rename or clean up these fields.
 */
public final class g implements Serializable {
    private static final long serialVersionUID = 16L;

    private String a;
    private int aid;
    private boolean b;
    private int c;
    private int d;
    private int e;
    private int f;
    private int g;
    private int h;
    private int hash;
    private int i;
    private boolean j;
    private int sid;
    private int tid;

    public String legacyName() { return a; }
    public int legacyAge() { return d; }
    public int legacyCountry() { return c; }
    public int legacyPosition() { return e; }
    public int legacyStatus() { return f; }
    public int legacySide() { return i; }
    public int legacyCr1() { return g; }
    public int legacyCr2() { return h; }
    public int legacyAid() { return aid; }
    public int legacySid() { return sid; }
    public int legacyTid() { return tid; }
    public int legacyHash() { return hash; }
    public boolean legacyStar() { return b; }
    public boolean legacyWorldTop() { return j; }
}
