
import gulp from "gulp";
import ts from "gulp-typescript";
const tsProject = ts.createProject('tsconfig.json');
import jest from 'gulp-jest';

gulp.task('test', () => {
	return gulp.src('src').pipe(jest());
});

gulp.task('js', () => {
	const reporter = ts.reporter.fullReporter();
    const tsResult = tsProject.src()
        .pipe(tsProject(reporter));
    return tsResult.js
        .pipe(gulp.dest("dist"));
});

gulp.task("build", gulp.series("js", "test"));
gulp.task("default", gulp.series("build"));