import os
from glob import glob
here='C:/Users/marti/Desktop/rednorte-frontnend/rednorte-front/src'
for path in glob(here+'/**/*.ts', recursive=True) + glob(here+'/**/*.vue', recursive=True):
    with open(path, 'r', encoding='utf-8') as f:
        c = f.read()
    c = c.replace('id: number', 'id: string | number').replace('pacienteId: number', 'pacienteId: string | number').replace('especialidadId: number', 'especialidadId: string | number').replace('ref<number | null>(null)', 'ref<string | number | null>(null)')
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)
