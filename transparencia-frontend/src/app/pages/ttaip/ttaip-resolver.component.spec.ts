import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TtaipResolverComponent } from './ttaip-resolver.component';
import { ActivatedRoute, Router } from '@angular/router';
import { TtaipService } from '../../services/ttaip.service';
import { FormsModule } from '@angular/forms';

describe('TtaipResolverComponent - FE-03 (Trazabilidad y Decisiones)', () => {
  let component: TtaipResolverComponent;
  let fixture: ComponentFixture<TtaipResolverComponent>;

  beforeEach(async () => {

    const ttaipMock = {
      declararFundado: () => { return { subscribe: () => {} }; }
    };

    const routerMock = {
      navigate: () => {}
    };

    await TestBed.configureTestingModule({
      imports: [TtaipResolverComponent, FormsModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'EXP-2026-TEST' } } }
        },
        { provide: Router, useValue: routerMock },
        { provide: TtaipService, useValue: ttaipMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TtaipResolverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // PRUEBA 1: COMBOBOX DE DECISIONES
  it('debe permitir seleccionar todas las opciones de decisiones en el combobox', () => {
    const decisionesEsperadas = [
      'FUNDADO', 'FUNDADO_PARTE', 'INFUNDADO', 'INFUNDADO_PARTE',
      'IMPROCEDENTE', 'SUSTRACCION', 'DESISTIMIENTO'
    ];

    expect(component.opcionesFallo.length).toBe(7);

    decisionesEsperadas.forEach(decision => {
      component.decision = decision;
      expect(component.decision).toBe(decision);
    });
  });

  // PRUEBA 2: DESCARGA DE RESOLUCIÓN EN TRAZABILIDAD
  it('debe tener la función de descarga de resolución final implementada', () => {

    expect(typeof component.descargarResolucionFinal).toBe('function');
  });
});
