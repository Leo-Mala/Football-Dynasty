package est;

import java.io.Serializable;

/** Legacy .ai21 metadata serialization shell. */
public final class InfoArquivoSalvoType implements Serializable {
    private static final long serialVersionUID = 1L;

    private String n;
    private String tc;
    private Integer a;
    private String i;
    private String path;

    public Integer legacyA() { return a; }
    public String legacyI() { return i; }
    public String legacyN() { return n; }
    public String legacyPath() { return path; }
    public String legacyTc() { return tc; }
}
