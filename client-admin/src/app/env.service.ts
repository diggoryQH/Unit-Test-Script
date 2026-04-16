import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class EnvService {
  private envSubject: BehaviorSubject<any> = new BehaviorSubject<any>({});
  public env$: Observable<any> = this.envSubject.asObservable();

  constructor(private http: HttpClient) {}

  loadEnv() {
    return this.http.get('/assets/env/.env', { responseType: 'text' }).pipe(
      map((data: string) => {
        const env: any = {};
        const lines = data.split('\n');
        lines.forEach(line => {
          const [key, value] = line.split('=');
          if (key && value) {
            env[key.trim()] = value.trim();
          }
        });
        this.envSubject.next(env);
        return env;
      })
    );
  }

  getEnv(key: string): string {
    return this.envSubject.value[key];
  }
}