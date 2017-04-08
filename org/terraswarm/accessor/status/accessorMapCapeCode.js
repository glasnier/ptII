// @version: $$Id$$
// Calculate maps of accessors to test cases and accessors to hosts, by 
// examining the contents of the accessors repository.
// Requires the glob package, 
// npm install -g glob
// https://github.com/isaacs/node-glob


var fs = require('fs');
var glob = require('glob');

// To run, invoke calculate().  Must be run from $PTII
module.exports = (function () {

    var baseDir = "./ptolemy/actor/lib/jjs/modules";
    var resultsFile = "./org/terraswarm/accessor/status/accessorMapCapeCode.txt";

    var testsToAccessors = {};
    var testsError = [];
    var accessorsToModules = {};
    var accessorsError = [];
    var hostsToModules = {};
    var hostsError = [];

    var testcases = [];
    var accessors = [];

    /** calculate() determines the set of accessors that are fully functional
     *  on each host.
     *  This is a three-step process.  The steps are independent; any ordering 
     *  is fine.
     *  
     *  1) Determine the modules supported by each host.
     *  
     *  2) Determine the modules required by each accessor.  
     *     Now we know the accessors supported by each host.
     *     
     *  3) Determine the accessors used by each test case.  
     *     This gives which accessors are fully functional on each host.
     */
    var calculate = function () {
        findTestCases();
        findAccessors();

        // Host to modules:
        // Scan ptolemy/actor/lib/jjs//modules subdirectories to ascertain 
        // supported modules.
        // So far, only browser and node offer additional modules.
        // hosts/browser/modules
        // hosts/common/modules (these are therefore supported by all hosts).
        // hosts/node/node_modules may include locally-installed modules.
        scanHosts(baseDir);
    };

    /** Check results objects for number of keys equal to expected length.
     * fs.readFile() doesn't return a promise, so can't wait on set of promises.
     */
    var checkIfDone = function () {
        // Assume at least one test case.  Array may not be populated before 
        // this is checked.
        if (Object.keys(testsToAccessors).length === (testcases.length - testsError.length) &&
            testcases.length > 0) {
            if (Object.keys(accessorsToModules).length ===
                (accessors.length - accessorsError.length)) {

                // Calculate accessors to hosts
                var accessorsToHosts = {};
                var hosts, modules, hasAllModules;

                for (var accessor in accessorsToModules) {
                    hosts = [];

                    modules = accessorsToModules[accessor];
                    if (modules !== null && typeof modules !== 'undefined') {
                        if (modules.length > 0) {
                            for (var host in hostsToModules) {
                                hasAllModules = true;

                                modules.forEach(function (module) {
                                    hostsToModules[host].includes(module);

                                    // FIXME:  Add common host.
                                    /*
                                    if (!hostsToModules[host].includes(module) && 
                                    		!hostsToModules.common.includes(module)) {
                                    	hasAllModules = false;
                                    }
                                    */

                                    if (!hostsToModules[host].includes(module)) {
                                        hasAllModules = false;
                                    }
                                });

                                if (hasAllModules) {
                                    hosts.push(host);
                                }
                            }

                            accessorsToHosts[accessor] = hosts;
                        } else {
                            // Accessors with no modules are supported by all.
                            accessorsToHosts[accessor] = ['all'];
                        }
                    }
                }

                var results = {};
                results.testsToAccessors = testsToAccessors;
                results.accessorsToHosts = accessorsToHosts;

                fs.writeFile(resultsFile, JSON.stringify(results), 'utf8', function (err) {
                    if (err) {
                        console.log('Error writing results file: ' + err);
                    }
                });


            }
        }
    }

    /** Find all accessor source files.
     */
    var findAccessors = function () {
        // Accessors are located in org/terraswarm/accessor/accessors/web
        // Find *.js files not in /test/auto or in any excluded directory.
        glob('./org/terraswarm/accessor/accessors/web/!(demo|hosts|jsdoc|library|obsolete|reports|styles|wiki)**/*.js', function (err, files) {
            accessors = files; // So we can check all finished later.
            // Accessors to modules:
            // Any file not under a /test/auto directory with a .js extension.
            // Look for require() statements.
            files.forEach(function (filepath) {
                scanAccessorFile(filepath);
            });
        });
    };

    /** Find all test case files.
     */
    var findTestCases = function () {
        // Test cases are located at:
        // ptolemy/actor/lib/jjs/modules/<modulename>/test/auto/*.xml
        // and 
        // org/terraswarm/accessor/test/auto/*.xml

        glob('./ptolemy/actor/lib/jjs/modules/**/test/auto/*.xml', function (err, files) {
            testcases.push.apply(testcases, files); // So we can check all finished later.
            files.forEach(function (filepath) {
                scanTestcase(filepath);
            });
        });

        glob('./org/terraswarm/accessor/test/auto/*.xml', function (err, files) {
            testcases.push.apply(testcases, files); // So we can check all finished later.

            files.forEach(function (filepath) {
                scanTestcase(filepath);
            });
        });
    };

    /** Record the names of required modules in the given accessor source file.
     * @param filepath The path to the accessor source file.
     */
    var scanAccessorFile = function (filepath) {

        fs.stat(filepath, function (err, stats) {
            if (err) {
                console.log(filepath + ' not found.');
                accessorsError.push(filepath);

            } else {
                fs.readFile(filepath, 'utf8', function (err, data) {
                    if (err) {
                        console.log('Error reading file ' + filepath + " : " + err);
                        accessorsError.push(filepath);
                    } else {

                        // Extract accessor name from filename. 
                        // Filepath is the full platform-dependent path.
                        // Extract part after */accessors/web/
                        var i = filepath.indexOf('accessors/web');
                        if (i >= 0) {
                            filepath = filepath.substring(i + 14);
                        }

                        accessorsToModules[filepath] = [];

                        // Look for matches to:
                        // require('something') where quotes may be double 
                        // quotes or single quotes.  Ignore whitespace.
                        // This will return, for example:
                        // require('cameras'
                        // Use /g at the end to find all matches.
                        var exp = /require\(\s*['"]\s*-*\w+-*\w*\s*['"]/g;

                        var matches = data.match(exp);

                        // Some accessors do not require any modules.
                        if (matches !== null && typeof matches !== 'undefined' &&
                            matches.length > 0) {
                            matches.forEach(function (match) {
                                // Module name is from ' or " to end-1
                                var quote = match.indexOf('\'');
                                if (quote < 0) {
                                    quote = match.indexOf('\"');
                                }
                                if (quote >= 0) {
                                    match = match.substring(quote + 1, match.length - 1);
                                }

                                accessorsToModules[filepath].push(match);
                            });
                        }
                    }
                });
            }
        });

        checkIfDone();
    };

    /** Scan the given directory for all subdirectories.  These subdirectories
     *  are the hosts.  Then, scan each host directory for modules.  Any *.js 
     *  file or subdirectory is considered a module.  Store results in 
     *  hostsToModules object.
     *  @param baseDir The directory containing host subdirectories.  
     */
    var scanHosts = function (baseDir) {

        var host = 'capecode';
        var modules = null;

        // TODO:  Add common host.
        try {
            var dirStats = fs.statSync(baseDir);
            if (dirStats !== null && typeof dirStats !== 'undefined' &&
                dirStats.isDirectory()) {
                modules = fs.readdirSync(baseDir);
            }

            hostsToModules[host] = [];

            if (modules !== null && typeof modules !== 'undefined') {
                // Modules are any *.js file or directory.
                var module;

                for (var i = 0; i < modules.length; i++) {
                    module = modules[i];
                    try {
                        var moduleStats =
                            fs.statSync(baseDir + "/" + module);
                        if (moduleStats !== null &&
                            moduleStats !== 'undefined' &&
                            moduleStats.isDirectory()) {
                            hostsToModules[host].push(module);
                        } else if (module.indexOf('.js') > 0 &&
                            module.indexOf('.js') === module.length - 3) {
                            hostsToModules[host].push(module.substring(0, module.length - 3));
                        }
                    } catch (err) {

                    }
                }
            }

        } catch (err) {
            console.log('Error tabulating modules for ' + host + '.');
            hostsError.push(host);
        }

        // Add native and common host modules to list.
        // FIXME:  Search common host directory instead of hard-coding modules.
        if (hostsToModules.capecode !== null &&
            typeof hostsToModules.capecode !== 'undefined') {
            hostsToModules.capecode.push('events');
            hostsToModules.capecode.push('querystring');
            hostsToModules.capecode.push('util');

        }

        checkIfDone();
    };

    /** Record the names of accessors used in the given test file.
     *  @param The path to the test file. 
     */
    var scanTestcase = function (filepath) {

        fs.stat(filepath, function (err, stats) {
            if (err) {
                // Record not-found test cases in a "failed" table.
                console.log(filepath + ' not found.');
                testsError.push(filepath);
            } else {
                fs.readFile(filepath, 'utf8', function (err, data) {
                    if (err) {
                        console.log('Error reading file ' + filepath + " : " + err);
                        testsError.push(filepath);
                    } else {
                        // Look for a match to:
                        // <property name="accessorSource" 
                        // with a value tag:
                        // value="path/to/accessorname.js
                        // e.g.:
                        // value="https://accessors.org/net/REST.js"

                        var exp = /<property name=['"]accessorSource['"]\s*class=['"]org.terraswarm.accessor.JSAccessor\$ActionableAttribute['"]\s*value=['"][A-Za-z//:/.]*['"]/g;

                        var matches = data.match(exp);
                        filepath = filepath.substring(2);

                        if (matches !== null && typeof matches !== 'undefined' &&
                            matches.length > 0) {
                            if (!testsToAccessors.hasOwnProperty(filepath)) {
                                testsToAccessors[filepath] = [];
                            }

                            matches.forEach(function (match) {
                                // Accessor path is the value=""
                                var value = match.indexOf('value');

                                if (value !== -1) {
                                    var fullpath = match.substring(value + 7);

                                    var index = fullpath.indexOf('accessors/');
                                    if (index !== -1) {
                                        var path = fullpath.substring(index + 10,
                                            fullpath.length - 4);

                                        testsToAccessors[filepath].push(path);
                                    }
                                }
                            });

                        } else {
                            console.log('No accessors found in file: ' + filepath);
                            testsError.push(filepath);
                        }

                        checkIfDone();
                    }
                });
            }
        });

        checkIfDone();
    };

    return {
        calculate: calculate
    };
})();
