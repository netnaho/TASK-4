import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-feature-placeholder',
  standalone: true,
  imports: [MatCardModule, MatIconModule],
  templateUrl: './feature-placeholder.component.html',
  styleUrl: './feature-placeholder.component.scss'
})
export class FeaturePlaceholderComponent {
  readonly title = this.route.snapshot.data['title'] as string;

  constructor(private readonly route: ActivatedRoute) {}
}
