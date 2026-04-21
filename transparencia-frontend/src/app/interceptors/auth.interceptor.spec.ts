import { PLATFORM_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  afterEach(() => {
    localStorage.removeItem('usuario');
  });

  function runInterceptor(platformId: string, request: HttpRequest<unknown>) {
    TestBed.configureTestingModule({
      providers: [{ provide: PLATFORM_ID, useValue: platformId }],
    });

    const next = vi.fn((req: HttpRequest<unknown>) =>
      of(new HttpResponse({ status: 200, body: req.url })),
    );

    TestBed.runInInjectionContext(() => {
      authInterceptor(request, next).subscribe();
    });

    return next;
  }

  it('agrega header Authorization cuando existe token en localStorage', () => {
    localStorage.setItem('usuario', JSON.stringify({ token: 'jwt-token-test' }));
    const request = new HttpRequest('GET', '/api/privado');

    const next = runInterceptor('browser', request);
    const forwarded = next.mock.calls[0]?.[0] as HttpRequest<unknown>;

    expect(next).toHaveBeenCalledTimes(1);
    expect(forwarded.headers.get('Authorization')).toBe('Bearer jwt-token-test');
  });

  it('omite Authorization cuando no hay token en localStorage', () => {
    localStorage.removeItem('usuario');
    const request = new HttpRequest('GET', '/api/publico');

    const next = runInterceptor('browser', request);
    const forwarded = next.mock.calls[0]?.[0] as HttpRequest<unknown>;

    expect(next).toHaveBeenCalledTimes(1);
    expect(forwarded.headers.has('Authorization')).toBe(false);
  });

  it('en SSR no agrega Authorization aunque exista localStorage', () => {
    localStorage.setItem('usuario', JSON.stringify({ token: 'jwt-ssr' }));
    const request = new HttpRequest('GET', '/api/ssr');

    const next = runInterceptor('server', request);
    const forwarded = next.mock.calls[0]?.[0] as HttpRequest<unknown>;

    expect(next).toHaveBeenCalledTimes(1);
    expect(forwarded.headers.has('Authorization')).toBe(false);
  });
});
