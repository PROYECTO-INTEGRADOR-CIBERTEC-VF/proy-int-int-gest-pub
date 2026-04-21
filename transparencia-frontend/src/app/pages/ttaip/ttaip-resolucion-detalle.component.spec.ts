import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TtaipResolucionDetalleComponent } from './ttaip-resolucion-detalle.component';

describe('TtaipResolucionDetalleComponent', () => {
  let component: TtaipResolucionDetalleComponent ;
  let fixture: ComponentFixture<TtaipResolucionDetalleComponent >;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TtaipResolucionDetalleComponent ],
    }).compileComponents();

    fixture = TestBed.createComponent(TtaipResolucionDetalleComponent );
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
