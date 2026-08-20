package e;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Serialization compatibility shell for the legacy .ban team record.
 * Field names and serialVersionUID intentionally match the legacy byte stream.
 */
public final class t implements Serializable {
    private static final long serialVersionUID = 16L;

    private int a;
    private int aid;
    private int b;
    private int c;
    private String cor1;
    private String cor2;
    private String d;
    private String e;
    private String f;
    private int g;
    private String h;
    private int i;
    private int n;
    private transient String nomep;
    private int o;
    private int sid;
    private int tid;
    private int vid;
    private boolean valid;
    private int id;
    private ArrayList<g> l = new ArrayList<>();
    private ArrayList<g> m = new ArrayList<>();
    private transient boolean mark;

    public int legacyCountry() { return a; }
    public int legacyState() { return b; }
    public int legacyLevel() { return c; }
    public String legacyPrimaryColor() { return cor1; }
    public String legacySecondaryColor() { return cor2; }
    public String legacyFileRef() { return d; }
    public String legacyName() { return e; }
    public String legacyStadium() { return f; }
    public int legacyCapacity() { return g; }
    public String legacyCoach() { return h; }
    public int legacyCoachCountry() { return i; }
    public int legacyReputation() { return n; }
    public int legacyBaseColor() { return o; }
    public int legacyAid() { return aid; }
    public int legacySid() { return sid; }
    public int legacyTid() { return tid; }
    public int legacyVid() { return vid; }
    public int legacyId() { return id; }
    public boolean legacyValid() { return valid; }
    public ArrayList<g> legacyPlayers() { return l; }
    public ArrayList<g> legacyJuniors() { return m; }
}
