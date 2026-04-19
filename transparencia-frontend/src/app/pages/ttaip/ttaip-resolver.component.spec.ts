import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { TtaipResolverComponent } from './ttaip-resolver.component';
import { TtaipService } from '../../services/ttaip.service';

describe('TtaipResolverComponent', () => {
  let component: TtaipResolverComponent;
  let fixture: ComponentFixture<TtaipResolverComponent>;

  // Simulador del servicio
  const mockTtaipService = {
    declararFundado: () => of({ status: 'success' })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TtaipResolverComponent, RouterTestingModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => '00150-2025-JUS/TTAIP' } },
            paramMap: of({ get: () => '00150-2025-JUS/TTAIP' })
          }
        },
        {
          provide: TtaipService,
          useValue: mockTtaipService
        }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TtaipResolverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
