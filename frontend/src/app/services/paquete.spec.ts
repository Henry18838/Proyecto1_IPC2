import { TestBed } from '@angular/core/testing';

import { Paquete } from './paquete';

describe('Paquete', () => {
  let service: Paquete;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Paquete);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
