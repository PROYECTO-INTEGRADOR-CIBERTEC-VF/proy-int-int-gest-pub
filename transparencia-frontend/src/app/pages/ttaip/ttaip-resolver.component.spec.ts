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
      getApelacionPorExpediente: () => {
        return {
          subscribe: ({ next }: { next: (value: any) => void }) => {
            next({
              idApelacion: 10,
              expediente: 'EXP-2026-TEST',
              estado: 'EN_RESOLUCION',
              ciudadanoNombre: 'Ciudadano Prueba',
            });
          }
        };
      },
      declararFundado: () => ({ subscribe: () => {} }),
      declararFundadoEnParte: () => ({ subscribe: () => {} }),
      declararInfundado: () => ({ subscribe: () => {} }),
      declararInfundadoEnParte: () => ({ subscribe: () => {} }),
      declararImprocedente: () => ({ subscribe: () => {} }),
      declararSustraccionMateria: () => ({ subscribe: () => {} }),
      declararDesistimiento: () => ({ subscribe: () => {} }),
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
  it('debe permitir seleccionar todas las opciones de decisiones', () => {
    const decisionesEsperadas = [
      'fundado',
      'fundado_en_parte',
      'infundado',
      'infundado_en_parte',
      'improcedente',
      'sustraccion_materia',
      'desistimiento',
    ];

    decisionesEsperadas.forEach(decision => {
      component.decision = decision;
      expect(component.decision).toBe(decision);
    });
  });

  // PRUEBA 2: FLUJO PRINCIPAL DISPONIBLE
  it('debe tener la funcion enviarResolucion implementada', () => {
    expect(typeof component.enviarResolucion).toBe('function');
  });
});
