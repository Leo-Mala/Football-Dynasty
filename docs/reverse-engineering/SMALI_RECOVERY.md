# SMALI_RECOVERY — Fase 1

A varredura completa encontrou **104 métodos internos** substituídos pelo decompilador por `UnsupportedOperationException("Method not decompiled...")`: 36 em `com.brasfoot.v2028`, 35 em `a`, 9 em `d`, 2 em `field` e 22 em `components`. Para **104/104** o arquivo SMALI correspondente foi localizado e a estrutura de controle foi verificada. Nenhuma implementação foi inventada.

Os números abaixo são instruções úteis e branches observados diretamente no bloco SMALI; métodos muito grandes exigem nova leitura semântica no momento da migração do subsistema.

## `com.brasfoot.v2028` — 36

- `ActivityPref.rE()` → `ActivityPref.smali`; 170 instr.; 19 branches
- `ActivityPenalty.rA()` → `ActivityPenalty.smali`; 37; 5
- `ActivityPenalty.rz()` → `ActivityPenalty.smali`; 131; 22
- `ActivityEstadio.onCreate(Bundle)` → `ActivityEstadio.smali`; 281; 12
- `ActivityField.fGet(int)` → `ActivityField.smali`; 162; 1
- `ActivityConvoca.q(a.p)` → `ActivityConvoca.smali`; 49; 7
- `ActivityEditor.pS()` → `ActivityEditor.smali`; 58; 9
- `DialogTatics.onCreate(Bundle)` → `DialogTatics.smali`; 172; 20
- `DialogFieldInfo.d(a.ac,int)` → `DialogFieldInfo.smali`; 147; 16
- `ActivityEscala.gL()` → `ActivityEscala.smali`; 223; 22
- `GfxCore.acessoColors(ArrayList,a.al)` → `GfxCore.smali`; 111; 25
- `GfxCore.checkClassico(Activity)` → `GfxCore.smali`; 112; 17
- `GfxCore.ligaInfo(a.ac)` → `GfxCore.smali`; 77; 12
- `GfxCore.maybeScheduleSuperCup()` → `GfxCore.smali`; 51; 17
- `GfxCore.scoutPickPais(a.ac,int)` → `GfxCore.smali`; 72; 20
- `GfxCore.sulEnsureDates(int)` → `GfxCore.smali`; 97; 17
- `GfxCore.uclEnsureDates(int)` → `GfxCore.smali`; 62; 13
- `GfxCore.uclNames(a.al,d.q,String[],Context)` → `GfxCore.smali`; 32; 11
- `ActivityProcura.a(a.p,a.ac,int)` → `ActivityProcura.smali`; 2; 0
- `ActivityJornal.addSecV(LinearLayout,String,int,int)` → `ActivityJornal.smali`; 54; 14
- `ActivityEscolhaTimes.E(String)` → `ActivityEscolhaTimes.smali`; 38; 9
- `DialogTimeRodada.sP()` → `DialogTimeRodada.smali`; 132; 12
- `ActivityJogo.onCreate(Bundle)` → `ActivityJogo.smali`; 138; 6
- `ActivityJogo.qy()` → `ActivityJogo.smali`; 96; 12
- `ActivityEditorTeam.pS()` → `ActivityEditorTeam.smali`; 168; 31
- `ActivityMainTeam.onStart()` → `ActivityMainTeam.smali`; 97; 15
- `ActivityClass.b(boolean,int)` → `ActivityClass.smali`; 2; 0
- `ActivityClass.cJ(int)` → `ActivityClass.smali`; 63; 12
- `ActivityClass.i(d.q)` → `ActivityClass.smali`; 613; 107
- `DialogIgrokInfo.onCreate(Bundle)` → `DialogIgrokInfo.smali`; 554; 28
- `ActivityTimes.a(a.p,a.ac,int)` → `ActivityTimes.smali`; 2; 0
- `ActivityLoad.qS()` → `ActivityLoad.smali`; 128; 13
- `StartOptions.qr()` → `StartOptions.smali`; 142; 19
- `ActivityResults.rQ()` → `ActivityResults.smali`; 114; 20
- `ActivityResults.l(a.t)` → `ActivityResults.smali`; 622; 67
- `ActivitySavedTatics.sa()` → `ActivitySavedTatics.smali`; 115; 9

## Pacote `a` — 35

- `a.p.bg(int)` 258/107; `a.p.ir()` 713/200; `a.p.is()` 100/25; `a.p.a(a.t,int,int)` 9/1; `a.p.a(String,int,boolean)` 9/1; `a.p.ib()` 69/44; `a.p.ie()` 117/37.
- `a.u.a(boolean,a.p,a.ac)` 49/0; `a.u.a(boolean,a.q,a.ac)` 49/0.
- `a.a.g(boolean)` 58/8; `a.k.gM()` 176/18; `a.y.kE()` 221/39.
- `a.af.f(a.t)` 101/5; `a.af.g(a.t)` 435/83; `a.m.d(Context)` 62/18.
- `a.f.a(a.p,a.ac,int)` 87/16; `a.f.a(a.ac,int[])` 87/16; `a.ag.nk()` 27/7.
- `a.q.a(a.ac,int,a.q,int,String,Boolean)` 287/112; `a.q.o(a.ac)` 82/26; `a.q.p(a.ac)` 99/35.
- `a.c.<init>(a.al,a.al,a.ac,a.ac)` 10/0.
- `a.t.a(int,boolean,a.t,a.p,int,int,boolean)` 119/32; `a.t.b(a.t,boolean)` 24/3; `a.t.e(a.t)` 132/22; `a.t.M(int,int)` 145/35; `a.t.a(a.p,int,int)` 119/32; `a.t.a(int,int,int,int,ArrayList)` 119/32; `a.t.hu()` 135/19; `a.t.jp()` 242/40; `a.t.ju()` 63/25.
- `a.ac.a(a.ac,a.t,int,int,boolean)` 126/23; `a.ac.a(a.al,int)` 126/23; `a.ac.b(a.al,int,int)` 237/11; `a.ab.a(a.ac,boolean,boolean)` 25/10.

## Pacote `d` — 9

- `d.y.eq(int)` 33/6; `d.y.b(Context,boolean)` 63/10.
- `d.m.<init>(int)` 14/0; `d.m.eq(int)` 111/26.
- `d.x.Z(boolean)` 187/31.
- `d.q.a(ArrayList,boolean,boolean,ArrayList)` 231/60; `d.q.xA()` 1187/157.
- `d.ac.a(d.x,ArrayList,int,boolean,int,int,int)` 188/18; `d.ac.a(boolean,boolean)` 188/18.

## `field` — 2

- `RangeSeekBar.i(float)` 14/0.
- `RangeSeekBar.onTouchEvent(MotionEvent)` 106/16.

## `components` — 22

- `components.u.getView(...)` 102/14; `a.getView(...)` 341/23; `o.getView(...)` 199/11; `b.getView(...)` 485/33; `k.a(...)` 100/15; `d.getView(...)` 159/9.
- `components.cn.a(a.m,a.p)` 31/5; `components.cn.w(a.p)` 384/69.
- `s.getView(...)` 432/47; `m.getView(...)` 352/35; `ap.getView(...)` 407/22; `aw.a(...)` 123/12; `q.getView(...)` 614/54; `ae.getView(...)` 220/44; `l.getView(...)` 373/35.
- `components.bd.a(int,boolean,String,String,ArrayList,boolean)` 100/14.
- `components.c.getView(...)` 405/25.
- `NumberFormat.j(String,String)` 129/12; `NumberFormat.f(double)` 197/57.
- `components.j.a(...)` 106/9; `t.getView(...)` 224/12; `r.getView(...)` 362/65.

## Resultado e regra de confiança

- **SMALI localizado: 104/104**.
- Métodos curtos e sem branches têm alta confiança estrutural.
- Métodos com centenas de instruções/branches têm estrutura recuperada, mas não recebem nomes semânticos inventados.
- A migração de cada subsistema deverá transformar esses blocos em pseudocódigo semântico junto com as classes chamadas e testes de caracterização.
- Os maiores hotspots são `d.q.xA()`, `a.p.ir()`, `ActivityResults.l(a.t)`, `ActivityClass.i(d.q)`, `DialogIgrokInfo.onCreate()` e `components.cn.w(a.p)`.

Isso corrige a visão inicial de apenas 36 stubs: aqueles 36 eram somente os arquivos do pacote visível `com.brasfoot.v2028`; a auditoria completa encontrou **104** no código interno total.