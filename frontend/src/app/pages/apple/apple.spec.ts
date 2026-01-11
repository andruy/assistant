import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Apple } from './apple';

describe('Apple', () => {
  let component: Apple;
  let fixture: ComponentFixture<Apple>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Apple]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Apple);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
