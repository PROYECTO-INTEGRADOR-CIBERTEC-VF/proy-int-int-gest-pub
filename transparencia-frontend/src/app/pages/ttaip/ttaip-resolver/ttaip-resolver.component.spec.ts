import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TtaipResolverComponent } from './ttaip-resolver.component';

describe('TtaipResolverComponent', () => {
  let component: TtaipResolverComponent;
  let fixture: ComponentFixture<TtaipResolverComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TtaipResolverComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TtaipResolverComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
