// ETSTCC-13074: Karma/Jasmine configuration for `ng test`.
// Run headless: ng test --watch=false --browsers=ChromeHeadless --code-coverage
// (Chrome or Chromium must be installed; set CHROME_BIN if it is not on the default path.)
// The 80% coverage gate applies to the new feedback code only; legacy files are excluded from the gate
// (they are still instrumented and listed in the coverage report).
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {},
      clearContext: false
    },
    jasmineHtmlReporter: {
      suppressAll: true
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/serviceforge-frontend'),
      subdir: '.',
      reporters: [{ type: 'html' }, { type: 'text-summary' }, { type: 'json-summary' }],
      check: {
        global: {
          statements: 80,
          lines: 80,
          excludes: [
            'src/main.ts',
            'src/environments/**',
            'src/app/app.component.ts',
            'src/app/app.module.ts',
            'src/app/parts/**',
            'src/app/technician-calendar/**',
            'src/app/technician-management/**',
            'src/app/technician-detail/**',
            'src/app/services/parts.service.ts',
            'src/app/services/technician.service.ts',
            'src/app/models/job.model.ts',
            'src/app/models/technician.model.ts'
          ]
        }
      }
    },
    reporters: ['progress', 'kjhtml'],
    browsers: ['ChromeHeadless'],
    restartOnFileChange: true
  });
};
