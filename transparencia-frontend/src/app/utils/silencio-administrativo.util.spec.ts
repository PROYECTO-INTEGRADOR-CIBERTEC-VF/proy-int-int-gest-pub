import { describe, expect, it } from 'vitest';
import {
  canSendManualResponse,
  isSilencioAdministrativo,
} from './silencio-administrativo.util';

describe('silencio-administrativo util', () => {
  it('retorna false cuando el contexto es null o undefined', () => {
    expect(isSilencioAdministrativo(null)).toBe(false);
    expect(isSilencioAdministrativo(undefined)).toBe(false);
    expect(canSendManualResponse(null)).toBe(false);
    expect(canSendManualResponse(undefined)).toBe(false);
  });

  it('detecta silencio administrativo por estado VENCIDA', () => {
    expect(isSilencioAdministrativo({ estado: 'VENCIDA' })).toBe(true);
  });

  it('detecta silencio administrativo ignorando mayusculas/minusculas', () => {
    expect(isSilencioAdministrativo({ estado: 'vencida' })).toBe(true);
    expect(
      isSilencioAdministrativo({
        estado: 'respondida',
        respuesta: { tipoRespuesta: 'silencio_administrativo' },
      }),
    ).toBe(true);
  });

  it('detecta silencio administrativo por diasRestantes negativos', () => {
    expect(
      isSilencioAdministrativo({
        estado: 'EN_REVISION',
        diasRestantes: -1,
      }),
    ).toBe(true);
  });

  it('detecta silencio administrativo por tipo de respuesta explicito', () => {
    expect(
      isSilencioAdministrativo({
        estado: 'RESPONDIDA',
        respuesta: { tipoRespuesta: 'SILENCIO_ADMINISTRATIVO' },
      }),
    ).toBe(true);
  });

  it('permite respuesta manual si no hay silencio ni respuesta previa', () => {
    expect(
      canSendManualResponse({
        estado: 'EN_REVISION',
        diasRestantes: 3,
      }),
    ).toBe(true);
  });

  it('bloquea respuesta manual si aplica silencio administrativo', () => {
    expect(
      canSendManualResponse({
        estado: 'VENCIDA',
        diasRestantes: -2,
      }),
    ).toBe(false);
  });

  it('bloquea respuesta manual si ya existe respuesta registrada', () => {
    expect(
      canSendManualResponse({
        estado: 'RESPONDIDA',
        respuesta: { tipoRespuesta: 'ENTREGA_TOTAL' },
      }),
    ).toBe(false);
  });

  it('permite respuesta manual si respuesta existe pero tipoRespuesta es vacio', () => {
    expect(
      canSendManualResponse({
        estado: 'EN_REVISION',
        respuesta: { tipoRespuesta: '' },
      }),
    ).toBe(true);
  });
});
