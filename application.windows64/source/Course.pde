void createCourse() {

  course = new int[300];
  int[] lines = new int[0];
  switch(diff) {
  case 0:
    lines = int(split(loadStrings("easy.trk")[0], ','));
    break;
  case 1:
    lines = int(split(loadStrings("medium.trk")[0], ','));
    break;
  case 2:
    lines = int(split(loadStrings("medium.trk")[0], ','));
    break;
  case 3:
    generateCourse();
    break;
  }


  for (int i = 0; i < lines.length; i++) {
    course[i] = lines[i];
  }
}

void generateCourse() {

  for (int i = 0; i<300; i++) {
    course[i] = 0;
  }

  boolean tunnel = false;
  int tunnelLenght = 0;
  int weight = 0;

  for (int i = 0; i<300; i++) {
    if (random(1) > 0.90 && !tunnel) {
      tunnel = true;
      tunnelLenght = int(random(0, 20));
    }

    if (tunnel) {
      course[i] += 1;
      tunnelLenght--;

      if (tunnelLenght == 0) {
        tunnel = false;
      }
    }

    if ((course[i] & 1) == 0) {
      if (random(1) > 0.92 && weight == 0) {
        course[i] += 4;
        weight = 2;
      } else if (random(1) > 0.70 && weight == 0) {
        course[i] += 2;
        weight = 2;
      } 
      if (random(1)> 0.8 && (course[i] & 1) == 0 && weight == 0) {
        course[i] += 8;
        weight = 3;
      }
    } else {
      if (random(1) > 0.65 && weight == 0) {

        switch(int(random(1, 6))) {
        case 1:
          course[i] += 16;
          weight = 2;
          break;
        case 2:
          course[i] += 32;
          weight = 2;
          if (random(1)> 0.66) {
            course[i] += 2;
            weight +=2;
          }
          break;
        case 3:
          course[i] += 64;
          weight = 2;
          break;
        case 4:
          course[i] += 128;
          weight = 2;
          break;
        case 5:
          course[i] += 256;
          weight = 3;
          break;
        }
      }
    }

    weight--;
    weight = constrain(weight, 0, 10);
  }

  for (int i = 0; i < 10; i++) {
    course[i] = 0;
  }
}
