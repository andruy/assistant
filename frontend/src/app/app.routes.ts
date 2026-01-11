import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Apple } from './pages/apple/apple';

export const routes: Routes = [
    { path: '', component: Home },
    { path: 'apple', component: Apple },
    { path: '**', redirectTo: '' }
];
