import { TestBed } from '@angular/core/testing';

import { Carga } from './carga';

describe('Carga', () => {
  let service: Carga;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Carga);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
