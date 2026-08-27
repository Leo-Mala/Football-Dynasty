# Football Dynasty — Roadmap ativo

Este arquivo é o ponto de entrada para o cronograma oficial do desenvolvimento restante.

## Documento canônico

Leia obrigatoriamente:

- `AGENTS.md`
- `docs/DATA_FREEZE.md`
- `docs/REMAINING_MILESTONES_ROADMAP.md`

O documento `docs/REMAINING_MILESTONES_ROADMAP.md` define o formato operacional, escopo, gates e critérios de conclusão dos grandes marcos restantes após a Fase 8.

## Baseline atual

- integração: `phase4/core-game-domain`
- merge certificado da Fase 8: `86d8344dce508916699b270692fd035aaf8b2bd1`
- corpus oficial: `Brasfoot.apk_Decompiler.com.zip`
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Grandes marcos restantes

- **Fases 9–10 — Marco A:** o mundo do futebol começa a funcionar como temporada real.
- **Fases 11–14 — Marco B:** o jogo vira um manager completo.
- **Fase 15 — Marco C:** garante que não esquecemos partes do Brasfoot.
- **Fase 16 — Marco D:** transforma tudo em carreira durável.
- **Fase 17 — Marco E:** transforma o núcleo técnico em aplicativo jogável.
- **Fase 18 — Marco F:** APK, testes reais e fechamento.

## Regra operacional

As fases internas **não devem ser tratadas como ciclos independentes de branch + PR + certificação completa**. Elas são subblocos dentro dos grandes marcos acima. Cada marco usa uma única branch/PR principal, trabalho em blocos grandes, checkpoints substanciais e uma certificação final agregada no `FINAL_HEAD`.

Ao abrir um novo chat ou sessão, primeiro confira o GitHub real e continue o marco ativo; não recrie branch/PR apenas porque o histórico da conversa não está disponível.