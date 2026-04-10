import { TestBed } from '@angular/core/testing';

import { Destino } from './destino';

describe('Destino', () => {
  let service: Destino;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Destino);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
