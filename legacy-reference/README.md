# Legacy Reference Boundary

This directory documents how the legacy application is used during reconstruction.

## What belongs here

- mapping notes;
- recovered pseudocode/behavior descriptions;
- method-level references needed to explain parity work;
- hashes or identifiers used to tie a reconstruction note to a specific legacy artifact.

## What does not belong here

Do not commit the decompiled APK archive, full decompiled source tree, full SMALI tree or legacy binary assets into the modern application source by default.

The legacy archive remains an external reconstruction reference. Only narrowly scoped material required for a documented technical migration should be introduced, and only after its necessity and redistribution status are understood.

## Rule for lost Java methods

When decompiled Java contains a `Method not decompiled` stub, use the corresponding SMALI as the behavioral reference. Document the recovered behavior before implementing the modern equivalent.
