
import gulp from "gulp";
import ts from "gulp-typescript";
const tsProject = ts.createProject('tsconfig.json');
import jest from 'gulp-jest';

// transpile from typescript
gulp.task('js', () => {
	const reporter = ts.reporter.fullReporter();
    const tsResult = tsProject.src()
        .pipe(tsProject(reporter));
    return tsResult.js
        .pipe(gulp.dest("dist"));
});

// run unit tests using jest
gulp.task('jest', () => {
	return gulp.src('src').pipe(jest());
});

gulp.task("test", gulp.series("js", "jest"));

gulp.task("default", gulp.series("js"));