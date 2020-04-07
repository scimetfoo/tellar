# Tellar

Tellar is Clojure library that makes working with deeply nested collections easier. Initially made to reduce some of the redundancies in fixtures and tests of a project by cutting the cost of the copying the collection multiple times over for each test with minor changes, Tellar helps modify deeply nested collections with the help of functions like `assoc-nth`, `assoc-all`, `dissoc-nth`, `dissoc-all`, and `update-all`. The function `trace` provides a trajectory for all occurrences of a given key.

## Usage


## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
